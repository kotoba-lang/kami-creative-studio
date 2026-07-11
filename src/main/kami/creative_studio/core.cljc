(ns kami.creative-studio.core)

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
