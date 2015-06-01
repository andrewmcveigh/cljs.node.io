(ns cljs.node.types.file
  (:refer-clojure :exclude [-name])
  (:require [cljs.nodejs :as node]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(declare file)

(defprotocol IFile
  (-absolute? [_])
  (-directory? [_])
  (-exists? [_])
  (-path [_])
  (-parent [_])
  (-name [_])
  (-delete! [_])
  (-touch! [_])
  (-mkdir! [_])
  (-mkdirs! [_])
  )

(deftype File [path-obj]
  IFile
  (-absolute? [this] (.isAbsolute path (-path this)))
  (-directory? [this]
    (and (-exists? this)
         (.isDirectory (.statSync fs (-path this)))))
  (-exists? [this] (.existsSync fs (-path this)))
  (-path [_] (.format path path-obj))
  (-parent [this]
    (let [dir (.dirname path (-path this))]
      (if (-name this)
        (file dir)
        (file (.resolve path path-obj "..")))))
  (-name [this] (.basename path (-path this)))
  (-delete! [this] (.unlinkSync fs (-path this)))
  (-touch! [this]
    (let [path (-path this)]
      (.closeSync fs (.openSync fs path "w"))
      this))
  (-mkdir! [this] (.mkdirSync fs (-path this)) true)
  (-mkdirs! [this]
    (let [parent (-parent this)]
      (cond (-directory? this) true
            (-exists? this) false
            (-directory? parent) (-mkdir! this)
            :else (and (-mkdirs! parent) (-mkdir! this)))))
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
