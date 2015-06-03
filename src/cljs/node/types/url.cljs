(ns cljs.node.types.url
  (:require
   [cljs.nodejs :as node]))

(def node-url (node/require "url"))

(defprotocol IURL
  (-absolute? [_])
  (-protocol [_])
  (-auth [_])
  (-host [_])
  (-port [_])
  (-hostname [_])
  (-pathname [_])
  (-search [_])
  (-query [_])
  (-hash [_]))

(deftype URL [url-obj]
  IURL
  (-absolute? [this] (or (-protocol this)
                         (-hostname this)
                         (-host this)
                         (re-find #"^/" (-pathname this))))
  (-protocol [_] (.-protocol url-obj))
  (-auth [_] (.-auth url-obj))
  (-host [_] (.-host url-obj))
  (-port [_] (.-host url-obj))
  (-hostname [_] (.-hostname url-obj))
  (-pathname [_] (.-pathname url-obj))
  (-search [_] (.-search url-obj))
  (-query [_] (.-query url-obj))
  (-hash [_] (.-hash url-obj))

  ISeqable
  (-seq [this]
    (list (-protocol this)
          (-auth this)
          (-host this)
          (-hostname this)
          (-pathname this)
          (-search this)
          (-query this)
          (-hash this)))

  Object
  (toString [_]
    (.format node-url url-obj)))

(defn parse [s]
  (URL. (.parse node-url s)))

(defn resolve [head & parts]
  (->> parts
       (remove nil?)
       (reduce #(.resolve node-url %1 %2) head)
       (parse)))
