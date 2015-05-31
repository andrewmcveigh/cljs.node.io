(ns cljs.node.io
  ^{:author "Andrew Mcveigh"
    :doc "This file defines polymorphic I/O utility functions for Clojurescript."}
  (:require-macros [cljs.node.io :refer [with-open]])
  (:require
   [cljs.nodejs :as node]
   [cljs.node.reader :as reader :refer [read-line]]
   [cljs.node.types :as types]
   [clojure.string :as string]))

(def Path (node/require "path"))
(def URL (node/require "url"))
(def Stream (node/require "stream"))
(def Readable (.-Readable Stream))
(def Writeable (.-Writeable Stream))
(def Buffer (node/require "buffer"))

(defprotocol Coercions
  "Coerce between various 'resource-namish' things."
  (as-file [x] "Coerce argument to a file.")
  (as-url [x] "Coerce argument to a URL."))

(defn- escaped-utf8-urlstring->str [s]
  (-> (clojure.string/replace s "+" (js/encodeURIComponent "+" "UTF-8"))
      (js/decodeURIComponent "UTF-8")))

(extend-protocol Coercions
  nil
  (as-file [_] nil)
  (as-url [_] nil)
  
  string
  (as-file [s] (types/file s))
  (as-url [s] (.parse URL s))
  
  types/File
  (as-file [f] f)
  (as-url [f] (.resolve URL "file://" (types/-path f)))

  ;; URL
  ;; (as-url [u] u)
  ;; (as-file [u]
  ;;   (if (= "file" (.-protocol u))
  ;;     (as-file (escaped-utf8-urlstring->str
  ;;               (.replace (.-pathname u) \/ (.-sep Path))))
  ;;     (throw (ex-info (str "Not a file: " u) {:type :illegal-argument}))))
  )

(defprotocol IOFactory
  "Factory functions that create ready-to-use, buffered versions of
  the various Java I/O stream types, on top of anything that can
  be unequivocally converted to the requested kind of stream.

  Common options include
   
  :append    true to open stream in append mode
  :encoding  string name of encoding to use, e.g. \"UTF-8\".

  Callers should generally prefer the higher level API provided by
  reader, writer, input-stream, and output-stream."
  (make-reader [x opts] "Creates a BufferedReader. See also IOFactory docs.")
  (make-writer [x opts] "Creates a BufferedWriter. See also IOFactory docs.")
  (make-input-stream [x opts] "Creates a BufferedInputStream. See also IOFactory docs.")
  (make-output-stream [x opts] "Creates a BufferedOutputStream. See also IOFactory docs."))

(defn reader
  [x & opts]
  (make-reader x (when opts (apply hash-map opts))))

(defn writer
  [x & opts]
  (make-writer x (when opts (apply hash-map opts))))

(defn- append? [opts]
  (boolean (:append opts)))

(defn- encoding [opts]
  (or (:encoding opts) "UTF-8"))

(defn- buffer-size [opts]
  (or (:buffer-size opts) 1024))

(def default-streams-impl
  {:make-reader (fn [x opts] (make-reader (make-input-stream x opts) opts))
   :make-writer (fn [x opts] (make-writer (make-output-stream x opts) opts))
   :make-input-stream (fn [x opts]
                        (throw (ex-info
                                (str "Cannot open <" (pr-str x) "> as an InputStream.")
                                {:type :illegal-argument})))
   :make-output-stream (fn [x opts]
                         (throw (ex-info
                                 (str "Cannot open <" (pr-str x) "> as an OutputStream.")
                                 {:type :illegal-argument})))})

(extend-protocol IOFactory
  types/File
  (make-reader [x opts]
    (apply reader/sync-file-reader (types/-path x) (mapcat identity opts)))

  string
  (make-reader [x opts]
    (make-reader (as-file x) opts)))

(defn as-relative-path
  "Take an as-file-able thing and return a string if it is
  a relative path, else IllegalArgumentException."
  [x]
  (let [f (as-file x)]
    (if (.isAbsolute f)
      (throw
       (ex-info (str f " is not a relative path") {:type :illegal-argument}))
      (.format f))))

(defn file
  "Returns a Path, passing each arg to as-file. Multiple-arg
  versions treat the first argument as parent and subsequent args as
  children relative to the parent."
  ([arg] 
   (as-file arg))
  ([parent child]
   (types/file (as-file parent) (as-relative-path child)))
  ([parent child & more]
   (reduce file (file parent child) more)))

(defn line-seq
  "Returns the lines of text from rdr as a lazy sequence of strings.
  rdr must implement cljs.node.reader.LineReader."
  [rdr]
  (when-let [line (read-line rdr)]
    (cons line (lazy-seq (line-seq rdr)))))

(comment
  
  ;;; read-line echo

  (enable-console-print!)
  (with-open [in reader/*in*]
    (doseq [line (line-seq in)]
      (println "=>" line)))
  
  )
