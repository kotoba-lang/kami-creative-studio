(ns kami.creative-studio.core
  (:require [kisekae.spec :as kisekae-spec]
            [kisekae.edit :as kisekae-edit]
            [kisekae.compositor :as compositor]
            [kotoba.lang.capability-values :as cap-values]))

(def stage-order [:model :rig :motion :music])

(def trait-order [:body :hair :face :outfit :accessory])

(defn select-trait
  "Select one asset per slot without coupling the composition domain to a renderer."
  [selection {:keys [slot id source]}]
  (assoc selection slot {:id id :source source}))

(defn selection-operations [selection]
  (->> trait-order
       (keep (fn [slot]
               (when-let [{:keys [id source]} (get selection slot)]
                 {:op/type "part/set" :part/kind (name slot)
                  :part/id id :part/source source})))
       vec))

(defn seeded-selection
  "Deterministic catalog selection. Same seed and catalog produce the same character."
  [catalog seed]
  (reduce
   (fn [selection [index slot]]
     (let [assets (vec (get catalog slot))]
       (if (seq assets)
         (assoc selection slot (nth assets (mod (+ seed (* 17 index)) (count assets))))
         selection)))
   {} (map-indexed vector trait-order)))

(defn- concrete-cap [kind resource]
  (cap-values/intersect-grants
   {:requested (cap-values/make-cap kind resource)
    :cacao-grants [{:grant/kind kind :grant/resources #{resource}
                    :grant/expires nil :grant/id "studio-local-user"}]
    :local-policy {:policy/allow {kind #{resource}}}
    :now "2026-07-11"}))

(defn composition-context
  "Turn visible selections into an authorized kisekae spec and portable plan.
   Body selects the base/skeleton; other slots select donor parts."
  [selection]
  (let [id "studio-character"
        base-url (get-in selection [:body :source])
        initial (kisekae-spec/new-spec {:id id :name "KAMI Character" :base-vrm-url base-url})
        spec (reduce (fn [s slot]
                       (if-let [source (get-in selection [slot :source])]
                         (if (= source base-url) s
                             (kisekae-edit/apply-op s {:op/type :op/add-part
                                                       :part {:part/kind slot
                                                              :part/source {:vrm/url source}}}))
                         s))
                     initial [:hair :face :outfit :accessory])
        output "blob:kisekae-preview"
        asset-urls (kisekae-spec/part-urls spec)
        caps (vec (concat
                   (map #(concrete-cap :vrm/asset-read %) asset-urls)
                   [(concrete-cap :vrm/compose id)
                    (concrete-cap :vrm/preview id)
                    (concrete-cap :vrm/export output)]))]
    {:spec spec :caps caps
     :plan (compositor/authorized-plan! caps {:spec spec :output-resource output
                                              :preview-target :character-canvas})}))

(defn murakumo-compose-request
  "Portable Cloud Murakumo generation envelope for an arbitrary selection."
  [selection]
  (let [{:keys [plan caps]} (composition-context selection)]
    {:schema "cloud.murakumo.gen-request/v1"
     :function "vrm-compose"
     :engine "kisekae"
     :modality "vrm-compose"
     :model "kisekae-v1"
     :params {:plan plan :caps caps}}))

(defn preview-artifact
  "Resolve the static real-VRM fixture for one donor override. Arbitrary
   multi-part combinations deliberately return nil and require Murakumo."
  [selection {:keys [constraint-url seed-url]}]
  (let [base (get-in selection [:body :source])
        overrides (filterv #(and (= seed-url (get-in selection [% :source]))
                                 (not= seed-url base))
                           [:hair :face :outfit])]
    (cond
      (and (= constraint-url base) (empty? overrides)) constraint-url
      (and (= seed-url base) (empty? overrides)) seed-url
      (and (= constraint-url base) (= 1 (count overrides)))
      (str "samples/constraint-seed-" (name (first overrides)) ".vrm")
      :else nil)))

(defn project-manifest
  [{:keys [id name brief reference motion-preset duration edits created-at]}]
  {:schema "kami.creative-project/v1"
   :id id
   :name name
   :brief brief
   :createdAt created-at
   :stages
   [{:id "model" :modality "3d" :model "trellis"
     :refs (cond-> [] (seq reference) (conj reference))
     :requires "remote-gpu-capability" :output ["glb" "gltf"]}
    {:id "rig" :modality "rig" :model "unirig" :from "model"
     :requires "remote-gpu-capability" :output ["vrm"]}
    {:id "motion" :modality "motion" :model "edn-motion-v1" :from "rig"
     :params {:preset (clojure.core/name motion-preset) :duration duration}
     :requires "mac-mini-control-worker" :output ["edn"]}
    {:id "music" :modality "music" :model "ace-step" :prompt brief
     :requires "remote-gpu-capability" :output ["wav"]}]
   :character {:editor "kotoba-lang/kisekae"
               :operations (vec edits)}
   :policy {:noSilentFallback true
            :artifactAddressing "CID"
            :credentialsInManifest false}})

(defn artifact-url
  "Extract a browser-renderable model URL from common Murakumo response shapes."
  [response]
  (or (:artifact-url response)
      (:artifactUrl response)
      (get-in response [:artifact :url])
      (some (fn [artifact]
              (when (contains? #{"vrm" "glb" "gltf" :vrm :glb :gltf}
                               (or (:kind artifact) (:format artifact)))
                (:url artifact)))
            (:artifacts response))))

(defn status-url [response]
  (or (:status-url response) (:statusUrl response) (get-in response [:job :status-url])))

(defn progress [response]
  (let [n (or (:progress response) (get-in response [:job :progress]) 0)]
    (-> n (max 0) (min 100))))
