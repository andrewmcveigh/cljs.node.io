(ns cljs.js.io
  (:require
   [cljs.js.url :as url :refer [Url]]
   [clojure.string :as string]
   [goog.Uri]))

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
  ;; (as-file [s] (file/file s))
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

;; (defn reader
;;   [x & opts]
;;   (make-reader x (when opts (apply hash-map opts))))

;; (defn writer
;;   [x & opts]
;;   (make-writer x (when opts (apply hash-map opts))))

(extend-protocol IOFactory
  Url
  (make-reader [x opts]
    (case (url/-protocol x)
      "file" (apply reader/sync-file-reader (as-file x) (mapcat identity opts))
      (throw (ex-info "Cannot create reader from a URL that does not represent a file"
                      {:type :illegal-argument}))))

  string
  (make-reader [x opts]
    (make-reader (as-file x) opts)))
