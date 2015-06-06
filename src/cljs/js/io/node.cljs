(ns cljs.js.io.node
  (:require
   [cljs.nodejs :as node]
   [cljs.js.io :as io :refer [IOFactory UrlReader]]
   [cljs.js.url :as url :refer [Url]]
   [clojure.string :as string]
   [goog.string]))

(defn xhr*
  [{:keys [url method content headers on-complete on-data on-error]}]
  (let [method (string/upper-case (name method))
        buf (goog.string.StringBuffer.)
        on-data (fn [request]
                  (.setEncoding request "UTF-8")
                  (.on request "data" (fn [chunk] (.append buf chunk)))
                  (.on request "end" (fn [] (on-complete (str buf)))))]
    (doto (js/require "https")
      (.get url on-data))))

(defn xhr
  [& {:keys [url method content headers on-complete on-data on-error] :as opts}]
  (xhr* opts))

(extend-protocol IOFactory
  Url
  (make-reader [x opts]
    (condp #(or (= %1 %2) (contains? %1 %2)) (url/-scheme x)
      #{"http" "https"} (UrlReader. x xhr nil)))
  
  )
