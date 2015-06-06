(ns cljs.js.io.browser
  (:require
   [cljs.js.io :as io :refer [Coercions IOFactory UrlReader]]
   [cljs.js.url :as url :refer [Url]]
   [clojure.string :as string]
   [goog.events :as events])
  (:import
   [goog.net XhrIo]
   [goog.net.EventType]
   [goog.events EventType]))

(defn xhr*
  [{:keys [url method content headers on-complete on-error]}]
  (let [method (string/upper-case (name method))
        xhr (XhrIo.)
        complete (fn [e] (on-complete (.getResponseText xhr)))]
    (when on-complete
      (events/listen xhr goog.net.EventType.COMPLETE complete))
    (.send xhr url method nil #js {})))

(defn xhr
  [& {:keys [url method content headers on-complete on-data on-error] :as opts}]
  (xhr* opts))

(extend-protocol Coercions
  string
  (as-file [s] nil))

(extend-protocol IOFactory
  Url
  (make-reader [x opts]
    (condp #(or (= %1 %2) (contains? %1 %2)) (url/-scheme x)
      #{"http" "https"} (UrlReader. x xhr nil)))
  
  )
