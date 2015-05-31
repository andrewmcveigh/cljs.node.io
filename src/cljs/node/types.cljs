(ns cljs.node.types
  (:require [cljs.nodejs :as node]))

(def fs (node/require "fs"))
(def path (node/require "path"))

(defprotocol IFile
  (-absolute? [_])
  (-exists? [_])
  (-path [_])
  (-filename [_])
  )

(deftype File [path-obj]
  IFile
  (-absolute? [_] (.isAbsolute path path-obj))
  (-exists? [_] (.existsSync fs path-obj))
  (-path [_] (.format path path-obj))
  (-filename [_] (.basename path path-obj))
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
