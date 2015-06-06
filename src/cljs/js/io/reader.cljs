(ns cljs.js.io.reader
  (:refer-clojure :exclude [with-open])
  (:require
   [cljs.js.io.stream
    :refer [Openable Closeable open close]
    :refer-macros [with-open]]
   [cljs.js.io.node.reader :refer [ReadableReader]]
   [clojure.string :as string]
   [goog.events :as events])
  (:import
   [goog.net XhrIo]
   [goog.net.EventType]
   [goog.events EventType]))

(defprotocol Reader
  (read-char [reader]
    "Returns the next char from the Reader, nil if the end of stream has been reached")
  (peek-char [reader]
    "Returns the next char from the Reader without removing it from the reader stream"))

(defprotocol LineReader
  (read-line [reader]
    "Returns the next line from the Reader, nil if the end of stream has been reached"))

(defprotocol IBufferedReader
  (-read [reader buffer offset length position]
    "Reads `length` bytes into `buffer` of `length`. Starts reading
    into `buffer` at `offset`. Reads from `position`."))

(defn newline?
  "Checks whether the character is a newline"
  [c]
  (or (identical? \newline c) (identical? "\n" c)))

(deftype StringReader
  [s s-len ^:unsynchronized-mutable s-pos]
  Reader
  (read-char [reader]
    (when (> s-len s-pos)
      (let [r (nth s s-pos)]
        (set! s-pos (inc s-pos))
        r)))
  (peek-char [reader]
    (when (> s-len s-pos)
      (nth s s-pos)))

  LineReader
  (read-line [reader]
    (let [buf (goog.string.StringBuffer.)]
      (loop [c (read-char reader)]
        (when c
          (if (newline? c)
            (str buf)
            (do
              (.append buf c)
              (recur (read-char reader)))))))))

#_(deftype BufferedReader [rdr length ^:unsynchronized-mutable buf]
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
          (let [bytes-read (-read reader buf 0 length nil)]
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

)

#_(defn xhr
  [{:keys [url method content headers on-complete on-error]}]
  (let [method (string/upper-case (name method))
        xhr (XhrIo.)
        complete (fn [e] (.log js/console e xhr) (on-complete xhr))]
    (when on-complete
      (events/listen xhr goog.net.EventType.COMPLETE complete))
    (.send xhr url method nil #js {})))



#_(deftype UrlReader [url ^:unsynchronized-mutable reader]
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
          {:on-complete #(with-open [r2 (ReadableReader. % 128 nil)]
                           (body r2))})))


  Closeable
  (close [_]))

#_(extend-protocol IPrintWithWriter
  UrlReader
  (-pr-writer [o writer opts]
    (-write writer (str "#<" `UrlReader " " (.-url o) ">"))))
