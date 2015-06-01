(ns cljs.node.core
  (:require
   [cljs.node.io :as io :refer-macros [with-open]]
   [cljs.node.reader :as reader :refer [read-line]]
   [goog.string]
   )
  )

(defn slurp
  "Opens a reader on f and reads all its contents, returning a string.
  See cljs.node.io/reader for a complete list of supported arguments."
  {:added "1.0"}
  [f & opts]
  (let [sb (goog.string.StringBuffer.)]
    (with-open [r (apply io/reader f opts)]
      (loop [c (reader/read-char r)]
        (if c
          (str sb)
          (do
            (.append sb c)
            (recur (reader/read-char r))))))))

(defn line-seq
  "Returns the lines of text from rdr as a lazy sequence of strings.
  rdr must implement cljs.node.reader.LineReader."
  [rdr]
  (when-let [line (read-line rdr)]
    (cons line (lazy-seq (line-seq rdr)))))
