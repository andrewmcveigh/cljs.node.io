(ns cljs.node.writer
  (:require
   [cljs.nodejs :as node]
   [cljs.node.types.file :as file]
   [cljs.node.types.stream :refer [Closeable Openable close open]]))

(defprotocol Writer
  (append! [_ data]
    "Appends the char or string to this writer")
  (flush! [_]
    "Flushes the stream")
  (write! [_ data] [_ data off len]
    "Writes data to stream, or optionally a portion of x"))

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

(defn sync-file-writer [file & {:keys [encoding] :or {encoding "UTF-8"}}]
  (SyncFileWriter. (node/require "fs") (file/-path file) nil encoding nil 0))
