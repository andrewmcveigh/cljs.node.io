(ns cljs.node.types.file
  (:require [cljs.nodejs :as node]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(defprotocol IFile
  (-absolute? [_])
  (-exists? [_])
  (-path [_])
  (-parent [_])
  (-filename [_])
  (-delete! [_])
  (-touch! [_])
  (-mkdir! [_])
  (-mkdirs! [_])
  )

(deftype File [path-obj]
  IFile
  (-absolute? [_] (.isAbsolute path path-obj))
  (-exists? [this] (.existsSync fs (-path this)))
  (-path [_] (.format path path-obj))
  (-parent [this]
    (let [dir (.dirname path path-obj)]
      (if (-filename this)
        dir
        (.resolve path path-obj ".."))))
  (-filename [_] (.basename path path-obj))
  (-delete! [this] (.unlinkSync fs (-path this)))
  (-touch! [this]
    (let [path (-path this)]
      (.closeSync fs (.openSync fs path "w"))
      this))
  (-mkdir! [_] (.mkdirSync path path-obj))
  (-mkdirs! [this]
    (let [parent (-parent this)]
      (if (-exists? this)
        (-mkdir! this)
        (-mkdirs! parent))))
  Object
  (toString [this] (-path this)))

(extend-protocol IPrintWithWriter
  File
  (-pr-writer [o writer opts]
    (-write writer (str "#<" `File " " o ">"))))

(defmulti file type)
(defmethod file File [file] file)
(defmethod file js/String [filename] (File. filename))
