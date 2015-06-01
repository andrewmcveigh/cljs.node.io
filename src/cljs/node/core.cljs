(ns cljs.node.core
  (:require
   [cljs.node.io :as io :refer-macros [with-open]]
   [cljs.node.types.reader :as reader :refer [read-line]]
   [cljs.node.types.writer :as writer :refer [write!]]
   [goog.string]))

(def ^:dynamic *in*
  (reader/stdin-reader))

(def ^:dynamic *out*
  (writer/stdout-writer))

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

(defn spit
  "Opposite of slurp.  Opens f with writer, writes content, then
  closes f. Options passed to cljs.node.io/writer."
  [f content & options]
  (with-open [w (apply io/writer f options)]
    (write! w (str content))))

(defn line-seq
  "Returns the lines of text from rdr as a lazy sequence of strings.
  rdr must implement cljs.node.reader.LineReader."
  [rdr]
  (when-let [line (read-line rdr)]
    (cons line (lazy-seq (line-seq rdr)))))
