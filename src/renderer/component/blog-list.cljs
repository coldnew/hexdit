(ns renderer.component.blog-list
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [secretary.core :as secretary]))

(def electron (js/require "electron"))
(def ipcRenderer (.-ipcRenderer electron))

(def modal-state (reagent/atom {:idx -1
                                :visible false
                                :blog-title nil}))

(defn show-modal [evt idx title]
  (.stopPropagation evt)
  (reset! modal-state {:idx idx
                       :visible true
                       :blog-title title}))

(defn hide-modal [evt]
  (.stopPropagation evt)
  (swap! modal-state assoc :visible false))

(defn open-blog [blog-info]
  (rf/dispatch-sync [:switch-blog blog-info])
  (.send ipcRenderer "open-blog")
  (secretary/dispatch! "/blog"))

(defn remove-blog [evt idx]
  (.stopPropagation evt)
  (rf/dispatch-sync [:remove-blog idx]))

(defn blog-control-button [idx title]
  [:div {:class "blog-control"}
   [:i {:class "edit fa fa-pencil"}]
   [:i {:class "remove fa fa-trash"
        :on-click #(show-modal % idx title)}]])

(defn blog-list-item [idx blog-info]
  (let [title (get blog-info "title")
        description (get blog-info "description")]
    [ant/card {:class "blog-item"
             :bodyStyle {:padding "15px"}
             :noHovering true
             :bordered false
             :extra (reagent/as-element
                      [blog-control-button idx title])
             :on-click #(open-blog blog-info)}
      [:h2 {:class "blog-title"} title]
      [:p {:class "blog-description"} description]]))

(defn blog-list-component []
  [:div {:class "list-content"}
    (map-indexed
      (fn [idx blog-info]
        ^{:key (str idx (get blog-info "path"))}
        [blog-list-item idx blog-info])
      @(rf/subscribe [:blog-list]))
    [ant/modal {:title "删除博客"
               :visible (:visible @modal-state)
               :onOk #(remove-blog % (:idx @modal-state))
               :onCancel #(hide-modal %)}
    [:p (str "是否删除博客『" (:blog-title @modal-state) "』？")]]])
