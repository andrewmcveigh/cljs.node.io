(ns cljs.node.reader
  (:require
   [cljs.nodejs :as node]
   [goog.array]))

(def fs (node/require "fs"))

(defprotocol Reader
  (read-char [reader]
    "Returns the next char from the Reader, nil if the end of stream has been reached")
  (peek-char [reader]
    "Returns the next char from the Reader without removing it from the reader stream"))

(defprotocol LineReader
  (read-line [reader]
    "Returns the next line from the Reader, nil if the end of stream has been reached"))

(defprotocol Openable
  (open [_ body]
    "Executes body in an \"Open\" context"))

(defprotocol Closeable
  (close [_]))

(defn newline?
  "Checks whether the character is a newline"
  [c]
  (or (identical? \newline c) (identical? "\n" c)))

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
  
  LineReader
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

(deftype StdinReader [reader fs]
  Reader
  (read-char [_] (read-char reader))
  (peek-char [_] (peek-char reader))

  LineReader
  (read-line [_] (read-line reader))

  Openable
  (open [_ body]
    (set! (.-fd reader) (.openSync fs "/dev/stdin" "rs"))
    (body))

  Closeable
  (close [_]
    (.closeSync fs (.-fd reader))
    (set! (.-fd reader) nil)))

(defn sync-file-reader [filename & {:keys [length] :or {length 128}}]
  (SyncFileReader. fs filename length nil nil))

(def *in* (StdinReader. (sync-file-reader "/dev/stdin" :length 1) fs))
