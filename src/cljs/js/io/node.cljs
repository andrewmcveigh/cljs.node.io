(ns cljs.js.io.node
  (:require
   [cljs.nodejs :as node]
   [cljs.js.io :as io :refer
    [Openable Closeable Reader ILineReader Writer IOFactory UrlReader
     open close read-char read-line peek-char newline?]]
   [cljs.js.url :as url :refer [Url]]
   [clojure.string :as string]
   goog.array
   goog.string))

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

(deftype ReadableReader [readable length ^:unsynchronized-mutable buf]
  Reader
  (read-char [reader]
    (letfn [(pop-char! []
              (let [c (aget buf 0)]
                (goog.array/removeAt buf 0)
                (when (zero? (.-length buf)) (set! buf nil))
                (char c)))]
      (if-not buf
        (do
          (set! buf (js/Buffer. length))
          (let [bytes-read (.read readable 1)]
            (when (> bytes-read 0)
              (when (< bytes-read length)
                (set! buf (.slice buf 0 bytes-read)))
              (pop-char!))))
        (pop-char!))))
  (peek-char [reader]
    (if-not buf
      (do
        (set! buf (js/Buffer. length))
        (let [bytes-read (.read readable 1)]
          (when (> bytes-read 0)
            (char (aget buf 0)))))
      (char (aget buf 0)))))

(deftype SyncFileReader
    [fs path length ^:unsynchronized-mutable fd ^:unsynchronized-mutable buf]
  Reader
  (read-char [reader]
    (assert fd "Reader not open, fd is not set")
    (letfn [(pop-char! []
              (let [c (aget buf 0)]
                (goog.array/removeAt buf 0)
                (when (zero? (.-length buf)) (set! buf nil))
                (char c)))]
      (if-not buf
        (do
          (set! buf (js/Buffer. length))
          (let [bytes-read (.readSync fs fd buf 0 length nil)]
            (when (> bytes-read 0)
              (when (< bytes-read length)
                (set! buf (.slice buf 0 bytes-read)))
              (pop-char!))))
        (pop-char!))))
  (peek-char [reader]
    (assert fd "Reader not open, fd is not set")
    (if-not buf
      (do
        (set! buf (js/Buffer. length))
        (let [bytes-read (.readSync fs fd buf 0 length nil)]
          (when (> bytes-read 0)
            (char (aget buf 0)))))
      (char (aget buf 0))))

  ILineReader
  (read-line [reader]
    (assert fd "Reader not open, fd is not set")
    (let [buf (goog.string.StringBuffer.)]
      (loop [c (read-char reader)]
        (when c
          (if (newline? c)
            (str buf)
            (do
              (.append buf c)
              (recur (read-char reader))))))))

  Openable
  (open [_ body]
    (set! fd (.openSync fs path "r"))
    (body))

  Closeable
  (close [_]
    (.closeSync fs fd)
    (set! fd nil)
    (set! buf nil)))

(deftype SyncFileWriter
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
