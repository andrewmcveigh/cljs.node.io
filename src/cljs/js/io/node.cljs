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

(deftype ReadableReader [readable])

(deftype FileWriter
  [fs path length enc ^:unsynchronized-mutable fd ^:unsynchronized-mutable pos]
  Writer
  (append! [_ data]
    (.writeSync fs fd data pos)
    (set! pos (+ pos (count data))))
  (flush! [_] (.fsyncSync fs fd))
  (write! [_ data] (.writeSync fs fd data))
  (write! [_ data off len] (.writeSync fs fd (subs data off len)))
  
  Openable
  (open [_ body]
    (set! fd (.openSync fs path "w"))
    (body))
  
  Closeable
  (close [_]
    (.closeSync fs fd)
    (set! fd nil)))
