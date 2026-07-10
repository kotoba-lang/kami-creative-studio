(ns build
  (:require [clojure.java.io :as io]
            [hiccup2.core :as h]
            [shadow.css.build :as css]))

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

(defn release [_]
  (build-css!)
  (spit (io/file "public/index.html") (str "<!doctype html>" (h/html shell)))
  (io/copy (io/file "kami.creative-project.schema.json")
           (io/file "public/kami.creative-project.schema.json"))
  (spit (io/file "public/.nojekyll") "")
  {:built ["public/index.html" "public/css/ui.css" "public/js/main.js"]})
