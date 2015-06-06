(ns cljs.js.io.node.reader
  (:require
   [cljs.nodejs :as node]
   [cljs.js.io.reader :refer
    [Reader LineReader StringReader read-char peek-char read-line newline?]]
   [cljs.js.io.stream :refer [Closeable Openable close open]]
   [goog.array]))

(defn xhr
  [{:keys [url method content headers on-complete on-data on-error]}]
  (let [method (string/upper-case (name method))
        buf (StringBuffer.)
        on-data (fn [request]
                  (.setEncoding request "UTF-8")
                  (.on request "data" (fn [chunk] (.append buf chunk)))
                  (.on request "end" (fn [] (on-complete (str buf)))))]
    (doto (js/require "https")
      (.get url on-data))))

(deftype UrlReader [url ^:unsynchronized-mutable reader]
  Reader
  (read-char [this]
    (assert reader "Reader not open, reader is not set")
    (read-char reader))
  (peek-char [this]
    (assert reader "Reader not open, reader is not set")
    (peek-char reader))

  LineReader
  (read-line [this] (read-line reader))

  Openable
  (open [this body] (open this body {}))
  (open [this body opts]
    (xhr (merge
          {:url (str url) :method :get}
          opts
          {:on-complete (fn [response]
                          (set! reader (StringReader. response (count response) 0))
                          (body))})))


  Closeable
  (close [_]))
