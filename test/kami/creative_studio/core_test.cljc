(ns kami.creative-studio.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [kami.creative-studio.core :as core]))

(deftest manifest-integrates-kisekae-and-motion
  (let [m (core/project-manifest
           {:id "project-1" :name "A" :brief "B" :reference "bafy-ref"
            :motion-preset :dance :duration 4 :created-at "2026-07-10T00:00:00Z"
            :edits [{:op/type :part/set :part/kind :hair :part/source "bafy-hair"}]})]
    (is (= "kami.creative-project/v1" (:schema m)))
    (is (= "kotoba-lang/kisekae" (get-in m [:character :editor])))
    (is (= "motion" (get-in m [:stages 2 :modality])))
    (is (= true (get-in m [:policy :noSilentFallback])))))

(deftest extracts-realtime-artifact-contract
  (testing "direct and artifact-list response shapes"
    (is (= "x.vrm" (core/artifact-url {:artifactUrl "x.vrm"})))
    (is (= "x.glb" (core/artifact-url {:artifacts [{:kind "image" :url "x.png"}
                                                     {:kind "glb" :url "x.glb"}]}))))
  (is (= 100 (core/progress {:progress 200})))
  (is (= 0 (core/progress {:progress -2}))))

(deftest composes-independent-trait-catalog
  (let [catalog {:body [{:id "body-a" :source "cid:body-a"}]
                 :hair [{:id "hair-a" :source "cid:hair-a"}
                        {:id "hair-b" :source "cid:hair-b"}]}
        selection (core/seeded-selection catalog 7)
        operations (core/selection-operations selection)]
    (is (= selection (core/seeded-selection catalog 7)))
    (is (= #{"body" "hair"} (set (map :part/kind operations))))
    (is (= "hair-a" (get-in (core/select-trait selection
                                               {:slot :hair :id "hair-a" :source "cid:hair-a"})
                             [:hair :id])))))

(deftest builds-authorized-kisekae-composition-context
  (let [base "https://assets.test/base.vrm"
        donor "https://assets.test/donor.vrm"
        {:keys [spec caps plan]}
        (core/composition-context
         {:body {:id "base" :source base}
          :hair {:id "hair" :source donor}
          :face {:id "base-face" :source base}
          :outfit {:id "base-outfit" :source base}
          :accessory {:id "none" :source nil}})]
    (is (= base (get-in spec [:spec/base :vrm/url])))
    (is (= [:hair] (mapv :part/kind (:spec/parts spec))))
    (is (every? (comp seq :cap/provenance) caps))
    (is (= :asset/fetch (get-in plan [:plan/phases 0 :phase])))
    (is (= :vrm/export (get-in plan [:plan/phases 8 :phase])))))

(deftest resolves-real-preview-artifacts-without-fallback
  (let [constraint "https://assets.test/constraint.vrm"
        seed "https://assets.test/seed.vrm"
        opts {:constraint-url constraint :seed-url seed}
        base {:body {:source constraint} :hair {:source constraint}
              :face {:source constraint} :outfit {:source constraint}}
        hair (assoc base :hair {:source seed})
        multi (assoc hair :outfit {:source seed})]
    (is (= constraint (core/preview-artifact base opts)))
    (is (= "samples/constraint-seed-hair.vrm" (core/preview-artifact hair opts)))
    (is (nil? (core/preview-artifact multi opts)) "multi-part requires Murakumo")))
