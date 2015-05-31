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
  (-mkdir! [_])
  (-mkdirs! [_])
  )

(deftype File [path-obj]
  IFile
  (-absolute? [_] (.isAbsolute path path-obj))
  (-exists? [_] (.existsSync fs path-obj))
  (-path [_] (.format path path-obj))
  (-parent [this]
    (let [dir (.dirname path path-obj)]
      (if (-filename this)
        dir
        (.resolve path path-obj ".."))))
  (-filename [_] (.basename path path-obj))
  (-delete! [_] (.unlinkSync fs (-path path-obj)))
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

(defn file
  ([filename]
   {:pre [(string? filename)]}
   (File. (.parse path filename)))
  ([parent child]
   (file (.resolve path parent child)))
  ([parent child & more]
   (reduce file (file parent child) more)))
