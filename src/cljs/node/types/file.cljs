(ns cljs.node.types.file
  (:refer-clojure :exclude [-name])
  (:require [cljs.nodejs :as node]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(declare file)

(defprotocol IFile
  (-absolute? [_])
  (-absolute-path [_])
  (-accessed [_])
  (-created [_])
  (-delete! [_])
  (-directory? [_])
  (-exists? [_])
  (-mkdir! [_])
  (-mkdirs! [_])
  (-modified [_])
  (-name [_])
  (-parent [_])
  (-path [_])
  (-touch! [_])
  )

(deftype File [path-obj]
  IFile
  (-absolute? [this] (.isAbsolute path (-path this)))
  (-absolute-path [this] (.resolve path (-path this)))
  (-accessed [this] (.-atime (.statSync fs (-path this))))
  (-created [this] (.-ctime (.statSync fs (-path this))))
  (-delete! [this] (.unlinkSync fs (-path this)))
  (-directory? [this]
    (and (-exists? this)
         (.isDirectory (.statSync fs (-path this)))))
  (-exists? [this] (.existsSync fs (-path this)))
  (-mkdir! [this] (.mkdirSync fs (-path this)) true)
  (-mkdirs! [this]
    (let [parent (-parent this)]
      (cond (-directory? this) true
            (-exists? this) false
            (-directory? parent) (-mkdir! this)
            :else (and (-mkdirs! parent) (-mkdir! this)))))
  (-modified [this] (.-mtime (.statSync fs (-path this))))
  (-name [this] (.basename path (-path this)))
  (-path [_] (.format path path-obj))
  (-parent [this]
    (let [dir (.dirname path (-path this))]
      (if (-name this)
        (file dir)
        (file (.resolve path path-obj "..")))))
  (-touch! [this]
    (let [path (-path this)]
      (.closeSync fs (.openSync fs path "w"))
      this))

  Object
  (toString [this] (-path this)))

(extend-protocol IPrintWithWriter
  File
  (-pr-writer [o writer opts]
    (-write writer (str "#<" `File " " o ">"))))

(defn file
  ([file]
   (if (instance? File file)
     file
     (File. (.parse path file))))
  ([parent child]
   (File. (.parse path (.resolve path (-path parent) child)))))

(def separator (.-sep path))
