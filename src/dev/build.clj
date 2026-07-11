(ns build
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            [hiccup2.core :as h]
            [shadow.css.build :as css])
  (:import [java.nio ByteBuffer ByteOrder]
           [java.util Base64]))

(def shell
  [:html {:lang "ja"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
    [:meta {:name "theme-color" :content "#080b12"}]
    [:meta {:name "description" :content "KAMI Creative Studio — realtime VRM generation, preview and character editing."}]
    [:title "KAMI Creative Studio"]
    [:link {:rel "stylesheet" :href "css/ui.css"}]
    [:script {:type "module" :src "vendor/model-viewer/model-viewer.min.js"}]]
   [:body
    [:div#app {:aria-live "polite"} "KAMI Creative Studio loading…"]
    [:script {:src "js/main.js"}]]])

(defn- build-css! []
  (-> (css/start)
      (css/index-path (io/file "src/main") {})
      (css/generate '{:ui {:entries [kami.creative-studio.ui]}})
      (css/minify)
      (css/write-outputs-to (io/file "public/css"))))

(defn- sample-bytes [{:keys [mesh/positions mesh/triangles]}]
  (let [position-count (* 3 (count positions))
        index-count (* 3 (count triangles))
        buffer (doto (ByteBuffer/allocate (+ (* 4 position-count) (* 2 index-count)))
                 (.order ByteOrder/LITTLE_ENDIAN))]
    (doseq [position positions, value position] (.putFloat buffer (float value)))
    (doseq [triangle triangles, value triangle] (.putShort buffer (short value)))
    (.array buffer)))

(defn- build-sample! []
  (let [{:keys [sample/id sample/name sample/brief sample/motion sample/duration
                mesh/positions mesh/triangles material/base-color character/operations] :as sample}
        (edn/read-string (slurp (io/resource "samples/kami-sample.edn")))
        bytes (sample-bytes sample)
        position-bytes (* 4 3 (count positions))
        encoded (.encodeToString (Base64/getEncoder) bytes)
        mins (mapv #(apply min (map % positions)) [first second #(nth % 2)])
        maxs (mapv #(apply max (map % positions)) [first second #(nth % 2)])
        gltf {:asset {:version "2.0" :generator "KAMI Creative Studio Hiccup build"}
              :extensionsUsed ["KHR_materials_unlit"]
              :scene 0 :scenes [{:nodes [0]}] :nodes [{:mesh 0 :name name}]
              :meshes [{:name name :primitives [{:attributes {:POSITION 0} :indices 1 :material 0}]}]
              :materials [{:name "KAMI Mint" :pbrMetallicRoughness {:baseColorFactor base-color
                                                                    :metallicFactor 0.05 :roughnessFactor 0.42}
                           :extensions {:KHR_materials_unlit {}}}]
              :buffers [{:byteLength (alength bytes)
                         :uri (str "data:application/octet-stream;base64," encoded)}]
              :bufferViews [{:buffer 0 :byteOffset 0 :byteLength position-bytes :target 34962}
                            {:buffer 0 :byteOffset position-bytes
                             :byteLength (- (alength bytes) position-bytes) :target 34963}]
              :accessors [{:bufferView 0 :componentType 5126 :count (count positions)
                           :type "VEC3" :min mins :max maxs}
                          {:bufferView 1 :componentType 5123 :count (* 3 (count triangles))
                           :type "SCALAR"}]}
        project {:schema "kami.creative-project/v1" :id id :name name :brief brief
                 :sample true :artifactUrl "samples/kami-sample.gltf"
                 :stages [{:id "model" :modality "3d" :model "procedural-octahedron"
                           :status "done" :output ["gltf"]}
                          {:id "rig" :modality "rig" :model "sample-static" :status "sample"}
                          {:id "motion" :modality "motion" :model "edn-motion-v1"
                           :params {:preset (clojure.core/name motion) :duration duration} :status "sample"}
                          {:id "music" :modality "music" :model "ace-step" :status "sample"}]
                 :character {:editor "kotoba-lang/kisekae" :operations operations}}]
    (.mkdirs (io/file "public/samples"))
    (spit (io/file "public/samples/kami-sample.gltf") (json/write-str gltf))
    (spit (io/file "public/samples/kami-sample.project.json") (json/write-str project))))

(defn release [_]
  (build-css!)
  (build-sample!)
  (spit (io/file "public/index.html") (str "<!doctype html>" (h/html shell)))
  (io/copy (io/file "kami.creative-project.schema.json")
           (io/file "public/kami.creative-project.schema.json"))
  (spit (io/file "public/.nojekyll") "")
  {:built ["public/index.html" "public/css/ui.css" "public/js/main.js"
           "public/samples/kami-sample.gltf" "public/samples/kami-sample.project.json"]})
