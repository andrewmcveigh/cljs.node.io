(ns cljs.js.io
  (:refer-clojure :exclude [with-open])
  (:require
   [cljs.js.url :as url :refer [Url]]
   [clojure.string :as string]
   [goog.string]
   [goog.Uri]))

(defprotocol Openable
  (open [_ body] [_ body opts] "Executes body in an \"Open\" context"))

(defprotocol Closeable
  (close [_]))

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

(defprotocol Writer
  (append! [_ data]
    "Appends the char or string to this writer")
  (flush! [_]
    "Flushes the stream")
  (write! [_ data] [_ data off len]
    "Writes data to stream, or optionally a portion of x"))

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

(deftype UrlReader [url xhr-fn ^:unsynchronized-mutable reader]
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
    (apply xhr-fn
     :url (str url)
     :method :get
     :on-complete (fn [response]
                    (set! reader (StringReader. response (count response) 0))
                    (body))
     (mapcat identity opts)))


  Closeable
  (close [_] (set! reader nil)))

(defprotocol Coercions
  "Coerce between various 'resource-namish' things."
  (as-file [x] "Coerce argument to a file.")
  (as-url [x] "Coerce argument to a URL."))

(defn- escaped-utf8-urlstring->str [s]
  (-> (string/replace s "+" (js/encodeURIComponent "+" "UTF-8"))
      (js/decodeURIComponent "UTF-8")))

(def separator \/)

(extend-protocol Coercions
  nil
  (as-file [_] nil)
  (as-url [_] nil)
  
  string
  (as-url [s]
    (let [u (Url. (goog.Uri. s))]
      (cond (url/-scheme u) u
            (url/-absolute? u) (apply url/resolve "file://" (seq u)))))
  
  ;; file/File
  ;; (as-file [f] f)
  ;; (as-url [f] (url/resolve "file://" (file/-path f)))

  Url
  (as-url [u] u)
  (as-file [{:keys [protocol pathname] :as u}]
    (if (= "file" protocol)
      (as-file (escaped-utf8-urlstring->str
                (string/replace pathname \/ separator)))
      (throw (ex-info (str "Not a file: " u) {:type :illegal-argument})))))

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

(extend-protocol IOFactory
  ;; Url
  ;; (make-reader [x opts]
  ;;   (case (url/-protocol x)
  ;;     "file" (apply reader/sync-file-reader (as-file x) (mapcat identity opts))
  ;;     (throw (ex-info "Cannot create reader from a URL that does not represent a file"
  ;;                     {:type :illegal-argument}))))

  string
  (make-reader [x opts]
    (make-reader (as-file x) opts)))

(defn slurp
  "Opens a reader on f and reads all its contents, returning a string.
  See cljs.io/reader for a complete list of supported arguments."
  {:added "1.0"}
  [f & opts]
  (let [sb (goog.string.StringBuffer.)]
    (with-open [r (apply reader f opts)]
      (loop [c (read-char r)]
        (if c
          (do
            (.append sb c)
            (recur (read-char r)))
          (str sb))))))

(defn spit
  "Opposite of slurp.  Opens f with writer, writes content, then
  closes f. Options passed to cljs.node.io/writer."
  [f content & options]
  (with-open [w (apply writer f options)]
    (write! w (str content))))

(defn line-seq
  "Returns the lines of text from rdr as a lazy sequence of strings.
  rdr must implement cljs.node.reader.LineReader."
  [rdr]
  (when-let [line (read-line rdr)]
    (cons line (lazy-seq (line-seq rdr)))))
