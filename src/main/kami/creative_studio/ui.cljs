(ns kami.creative-studio.ui
  (:require [clojure.string :as str]
            [cljs.pprint]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [shadow.css :refer [css]]
            [kami.creative-studio.core :as core]))

(def $body (css {:margin "0" :background "#080b12" :color "#f0f3f8"
                 :font-family "Inter,ui-sans-serif,system-ui,-apple-system,Hiragino Sans,sans-serif"}))
(def $top (css {:min-height "64px" :display "flex" :align-items "center" :justify-content "space-between"
                :padding "0 24px" :border-bottom "1px solid #253044" :background "#090d15" :position "sticky" :top "0" :z-index "5"}))
(def $brand (css {:font-weight "900" :letter-spacing ".08em" :color "#f0f3f8" :text-decoration "none"}))
(def $actions (css {:display "flex" :gap "8px" :align-items "center"}))
(def $badge (css {:font-size "10px" :font-weight "800" :letter-spacing ".12em" :border "1px solid #33415a" :border-radius "20px" :padding "6px 9px"}))
(def $workspace (css {:display "grid" :grid-template-columns "minmax(300px,360px) 1fr" :min-height "calc(100vh - 64px)"}
                     ["@media(max-width:850px)" {:grid-template-columns "1fr"}]))
(def $aside (css {:padding "36px 28px" :border-right "1px solid #253044" :background "#0a0f18"}
                 ["@media(max-width:850px)" {:border-right "0" :border-bottom "1px solid #253044"}]))
(def $eyebrow (css {:font-family "ui-monospace,monospace" :font-size "10px" :font-weight "800" :letter-spacing ".18em" :color "#80ead0"}))
(def $title (css {:font-size "clamp(30px,4vw,46px)" :line-height "1.13" :margin "10px 0 14px"}))
(def $muted (css {:color "#8c98aa" :line-height "1.6"}))
(def $form (css {:display "grid" :gap "14px" :margin-top "28px"}))
(def $label (css {:display "grid" :gap "6px" :font-size "12px" :font-weight "700" :color "#b9c1ce"}))
(def $input (css {:width "100%" :box-sizing "border-box" :border "1px solid #253044" :background "#0b1019" :color "#f0f3f8"
                  :border-radius "8px" :padding "11px 12px" :font "inherit"}
                 ["&:focus" {:outline "1px solid #80ead0" :border-color "#80ead0"}]))
(def $button (css {:border "1px solid #33415a" :background "transparent" :color "#f0f3f8" :border-radius "8px" :padding "10px 14px"
                   :font-weight "800" :cursor "pointer"}
                  ["&:hover" {:border-color "#80ead0"}] ["&:disabled" {:opacity "0.35" :cursor "not-allowed"}]))
(def $primary (css {:background "#80ead0" :border-color "#80ead0" :color "#07110e"}))
(def $main (css {:min-width "0" :padding "32px clamp(18px,4vw,52px) 100px" :position "relative"}))
(def $head (css {:display "flex" :justify-content "space-between" :align-items "end" :gap "12px" :margin-bottom "24px"}))
(def $preview (css {:max-width "900px" :margin "0 auto 26px" :border "1px solid #253044" :border-radius "14px" :overflow "hidden" :background "#0d141f"}))
(def $preview-head (css {:display "flex" :justify-content "space-between" :align-items "center" :padding "14px 18px" :border-bottom "1px solid #253044"}))
(def $viewer (css {:width "100%" :height "430px" :background "radial-gradient(circle at 50% 42%,#263247,#101621 50%,#090d14)"}
                  ["@media(max-width:650px)" {:height "330px"}]))
(def $empty (css {:height "100%" :display "grid" :place-content "center" :text-align "center" :gap "6px" :color "#8c98aa"}))
(def $preview-actions (css {:display "grid" :grid-template-columns "auto auto 1fr auto" :gap "10px" :align-items "end" :padding "14px 18px"}
                           ["@media(max-width:650px)" {:grid-template-columns "1fr"}]))
(def $file (css {:border "1px solid #33415a" :border-radius "8px" :padding "11px 14px" :cursor "pointer" :position "relative" :overflow "hidden"}))
(def $hidden-file (css {:position "absolute" :opacity "0" :width "1px" :height "1px"}))
(def $progress (css {:height "26px" :background "#090d15" :position "relative" :border-top "1px solid #253044"}))
(def $progress-bar (css {:height "100%" :background "linear-gradient(90deg,#375f78,#80ead0)" :transition "width .25s"}))
(def $progress-label (css {:position "absolute" :inset "0" :display "grid" :place-content "center" :font "800 10px ui-monospace"}))
(def $tabs (css {:display "flex" :gap "6px" :margin "0 auto 18px" :max-width "900px"}))
(def $panel (css {:max-width "900px" :margin "0 auto" :border "1px solid #253044" :border-radius "14px" :padding "20px" :background "#0d141f"}))
(def $grid (css {:display "grid" :grid-template-columns "repeat(2,minmax(0,1fr))" :gap "12px"}
                 ["@media(max-width:650px)" {:grid-template-columns "1fr"}]))
(def $ops (css {:color "#8c98aa" :font "11px/1.7 ui-monospace" :padding-left "22px"}))
(def $pipeline (css {:display "grid" :grid-template-columns "repeat(4,1fr)" :gap "10px" :margin-top "22px"}
                     ["@media(max-width:700px)" {:grid-template-columns "1fr 1fr"}]))
(def $stage (css {:border "1px solid #253044" :border-radius "10px" :padding "13px" :background "#111824"}))
(def $stage-name (css {:font-size "10px" :color "#80ead0" :font-weight "800" :letter-spacing ".12em"}))
(def $runbar (css {:position "absolute" :left "0" :right "0" :bottom "0" :min-height "72px" :border-top "1px solid #253044" :background "#0b1019"
                  :display "flex" :align-items "center" :justify-content "space-between" :gap "12px" :padding "12px clamp(18px,4vw,52px)"}))
(def $toast (css {:position "fixed" :right "20px" :bottom "92px" :background "#172131" :border "1px solid #33415a" :padding "11px 15px" :border-radius "8px" :z-index "10"}))

(defonce state
  (r/atom {:name "Untitled character" :brief "" :reference "" :endpoint "" :artifact-url ""
           :motion-preset :dance :duration 4 :edits [] :tab :editor :status :ready :progress 0
           :manifest nil :toast nil :object-url nil}))

(defn setv! [k e] (swap! state assoc k (.. e -target -value)))
(defn notify! [s] (swap! state assoc :toast s) (js/setTimeout #(swap! state assoc :toast nil) 2200))

(defn manifest! []
  (let [s @state]
    (if (str/blank? (:brief s))
      (notify! "Creative briefを入力してください")
      (let [m (core/project-manifest
               {:id (str "project-" (.toString (js/Date.now) 36)) :name (:name s) :brief (:brief s)
                :reference (:reference s) :motion-preset (:motion-preset s) :duration (:duration s)
                :edits (:edits s) :created-at (.toISOString (js/Date.))})]
        (swap! state assoc :manifest m :status :planned)
        (notify! "制作planを更新しました") m))))

(defn load-artifact! [url]
  (when (seq url)
    (swap! state assoc :artifact-url url :progress 100 :status :preview)
    (notify! "3D artifactをpreviewへ読み込みました")))

(defn parse-json [response]
  (-> (.text response)
      (.then (fn [text]
               (try (js->clj (js/JSON.parse text) :keywordize-keys true)
                    (catch :default _ {:message text}))))))

(defn load-sample! []
  (-> (js/fetch "samples/kami-sample.project.json")
      (.then parse-json)
      (.then (fn [project]
               (swap! state assoc
                      :name (:name project)
                      :brief (:brief project)
                      :manifest project
                      :edits (vec (get-in project [:character :operations]))
                      :status :sample)
               (load-artifact! (core/artifact-url project))
               (notify! "sample projectを生成物から読み込みました")))
      (.catch (fn [e]
                (swap! state assoc :status :failed)
                (notify! (str "sample読込失敗: " (.-message e)))))))

(declare poll-job!)
(defn handle-job-response! [body]
  (when-let [url (core/artifact-url body)] (load-artifact! url))
  (swap! state assoc :progress (core/progress body))
  (when-let [url (core/status-url body)] (poll-job! url)))

(defn poll-job! [url]
  (js/setTimeout
   #(-> (js/fetch url)
        (.then parse-json)
        (.then (fn [body]
                 (handle-job-response! body)
                 (let [status (keyword (or (:status body) (get-in body [:job :status]) "running"))]
                   (swap! state assoc :status status)
                   (when-not (contains? #{:done :failed :cancelled} status) (poll-job! url)))))
        (.catch (fn [e] (swap! state assoc :status :failed) (notify! (str "進捗取得失敗: " (.-message e))))))
   1500))

(defn submit! []
  (when-let [m (or (:manifest @state) (manifest!))]
    (if (str/blank? (:endpoint @state))
      (notify! "Murakumo endpointを入力してください")
      (do (swap! state assoc :status :submitting :progress 1)
          (-> (js/fetch (:endpoint @state)
                        #js {:method "POST" :headers #js {"content-type" "application/json"}
                             :body (js/JSON.stringify (clj->js m))})
              (.then (fn [response] (if (.-ok response) (parse-json response) (throw (js/Error. (str "HTTP " (.-status response)))))))
              (.then (fn [body] (swap! state assoc :status :queued) (handle-job-response! body) (notify! "Murakumoへ送信しました")))
              (.catch (fn [e] (swap! state assoc :status :failed) (notify! (str "送信失敗: " (.-message e))))))))))

(defn file-change! [e]
  (when-let [file (aget (.. e -target -files) 0)]
    (when-let [old (:object-url @state)] (js/URL.revokeObjectURL old))
    (let [url (js/URL.createObjectURL file)]
      (swap! state assoc :object-url url) (load-artifact! url))))

(defn add-part! []
  (let [{:keys [part-kind part-source]} @state]
    (if (str/blank? (or part-source ""))
      (notify! "Donor URL / CIDを入力してください")
      (do (swap! state update :edits conj {:op/type "part/set" :part/kind (name (or part-kind :hair)) :part/source part-source})
          (manifest!) (notify! "kisekae part操作を追加しました")))))

(defn material! [e]
  (let [color (.. e -target -value)]
    (swap! state assoc :material-color color)
    (swap! state update :edits conj {:op/type "material/base-color" :value color})))

(defn field [label child] [:label {:class $label} label child])
(defn button [label on-click & [primary? disabled?]]
  [:button {:class (str $button (when primary? (str " " $primary))) :on-click on-click :disabled disabled? :type "button"} label])

(defn preview []
  (let [{:keys [artifact-url progress status]} @state]
    [:section {:class $preview}
     [:div {:class $preview-head}
      [:div [:div {:class $eyebrow} "REALTIME PREVIEW"] [:h3 "VRM / GLB Viewer"]]
      [:span {:class $badge} (str/upper-case (name status))]]
     [:model-viewer {:id "modelViewer" :class $viewer :src (when (seq artifact-url) artifact-url)
                     :camera-controls true :autoplay true :shadow-intensity "1" :environment-image "neutral"
                     :on-load #(swap! state assoc :status :preview :progress 100)}
      [:div {:slot "poster" :class $empty} [:strong "3D artifactを読み込む"] [:span "生成完了後は自動表示されます"]]]
     [:div {:class $preview-actions}
      [:label {:class $file} "VRM / GLBを開く" [:input {:class $hidden-file :type "file" :accept ".vrm,.glb,.gltf" :on-change file-change!}]]
      (button "Sampleを読込" load-sample!)
      (field "Artifact URL" [:input {:class $input :type "url" :value artifact-url :placeholder "https://…/character.vrm" :on-change #(setv! :artifact-url %)}])
      (button "表示" #(load-artifact! (:artifact-url @state)))]
     (when (and (pos? progress) (< progress 100))
       [:div {:class $progress} [:i {:class $progress-bar :style {:width (str progress "%")}}] [:span {:class $progress-label} (str progress "%")]])]))

(defn editor []
  (let [{:keys [edits]} @state]
    [:section {:class $panel}
     [:div {:class $eyebrow} "VRM CHARACTER EDITOR · kotoba-lang/kisekae"]
     [:h3 "Character composition"]
     [:div {:class $grid}
      (field "Part" [:select {:class $input :value (name (or (:part-kind @state) :hair)) :on-change #(swap! state assoc :part-kind (keyword (.. % -target -value)))}
                     (for [v ["hair" "face" "outfit" "accessory"]] ^{:key v} [:option {:value v} (str/capitalize v)])])
      (field "Donor URL / CID" [:input {:class $input :value (or (:part-source @state) "") :placeholder "bafy… / https://…" :on-change #(setv! :part-source %)}])
      (button "パーツ操作を追加" add-part!)
      (field "Material color" [:input {:class $input :type "color" :value (or (:material-color @state) "#ffffff") :on-change material!}])
      (field "Expression" [:select {:class $input :value (or (:expression @state) "happy") :on-change #(setv! :expression %)}
                           (for [v ["happy" "angry" "sad" "relaxed" "surprised"]] ^{:key v} [:option {:value v} (str/capitalize v)])])
      (field "Expression weight" [:input {:class $input :type "range" :min 0 :max 1 :step 0.01 :value (or (:expression-weight @state) 0)
                                         :on-change #(swap! state assoc :expression-weight (js/parseFloat (.. % -target -value)))}])
      (field "Motion" [:select {:class $input :value (name (:motion-preset @state)) :on-change #(swap! state assoc :motion-preset (keyword (.. % -target -value)))}
                       (for [v ["idle" "walk" "dance" "gesture"]] ^{:key v} [:option {:value v} (str/capitalize v)])])
      (field "Duration" [:input {:class $input :type "range" :min 1 :max 30 :value (:duration @state) :on-change #(swap! state assoc :duration (js/parseInt (.. % -target -value))) }])]
     [:ol {:class $ops}
      (if (seq edits)
        (map-indexed (fn [i op] ^{:key i} [:li (pr-str op)]) edits)
        [:li "編集操作はまだありません"])] ]))

(defn pipeline []
  [:div {:class $pipeline}
   (for [[id label detail] [[:model "MODEL" "TRELLIS"] [:rig "RIG" "UniRig"] [:motion "MOTION" "EDN retarget"] [:music "MUSIC" "ACE-Step"]]]
     ^{:key id} [:div {:class $stage} [:div {:class $stage-name} label] [:strong detail]])])

(defn app []
  (let [{:keys [name brief reference endpoint tab status toast]} @state]
    [:div
     [:header {:class $top} [:a {:class $brand :href "#"} "神 KAMI · CREATIVE STUDIO"]
      [:div {:class $actions} [:span {:class $badge} (if (str/blank? endpoint) "OFFLINE" "ENDPOINT READY")] (button "Plan更新" manifest!)]]
     [:main {:class $workspace}
      [:aside {:class $aside} [:div {:class $eyebrow} "PROJECT BRIEF"] [:h1 {:class $title} "生成から編集、previewまで。"]
       [:p {:class $muted} "Model、Rig、Motion、MusicをCIDで結び、VRMをブラウザ上でリアルタイムに確認します。"]
       [:div {:class $form}
        (field "プロジェクト名" [:input {:class $input :value name :on-change #(setv! :name %)}])
        (field "Creative brief" [:textarea {:class $input :rows 6 :value brief :on-change #(setv! :brief %)}])
        (field "参照画像 / CID" [:input {:class $input :value reference :on-change #(setv! :reference %)}])
        (field "Murakumo endpoint" [:input {:class $input :type "url" :value endpoint :placeholder "https://…/api/gen" :on-change #(setv! :endpoint %)}])
        (button "制作planを生成" manifest! true)]]
      [:section {:class $main}
       [:div {:class $head} [:div [:div {:class $eyebrow} "LIVE CHARACTER WORKSPACE"] [:h2 "Realtime Preview + VRM Editor"]]]
       [preview]
       [:div {:class $tabs}
        (button "Character Editor" #(swap! state assoc :tab :editor) (= tab :editor))
        (button "Manifest" #(swap! state assoc :tab :manifest) (= tab :manifest))]
       (if (= tab :editor) [editor]
           [:section {:class $panel} [:pre (with-out-str (cljs.pprint/pprint (:manifest @state)))]])
       [pipeline]
       [:footer {:class $runbar} [:div [:strong (str/upper-case (clojure.core/name status))] [:div {:class $muted} "Murakumo job + CID artifacts"]]
        (button "Murakumoで生成" submit! true (or (str/blank? endpoint) (= status :submitting)))]]]
     (when toast [:div {:class $toast} toast])]))

(defn ^:export init! []
  (set! (.-className js/document.body) $body)
  (rdom/render [app] (.getElementById js/document "app"))
  (load-sample!))
