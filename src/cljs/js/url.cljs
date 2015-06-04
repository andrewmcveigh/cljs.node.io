(ns cljs.js.url
  (:refer-clojure :exclude [-hash])
  (:require
   [clojure.string :as string]
   [goog.Uri]))

(defprotocol IUrl
  (-absolute? [_])
  (-scheme [_])
  (-auth [_])
  (-user [_])
  (-password [_])
  (-host [_])
  (-port [_])
  (-path [_])
  (-query [_])
  (-hash [_]))

(deftype Url [url]
  IUrl
  (-absolute? [this] (or (-scheme this)
                         (-host this)
                         (re-find #"^/" (-path this))))
  (-scheme [_] (.getScheme url))
  (-auth [_] (.getUserInfo url))
  (-user [_] (some-> url -auth (string/split #":") first))
  (-password [_] (some-> url -auth (string/split #":") second))
  (-host [_] (.getDomain url))
  (-port [_] (.getPort url))
  (-path [_] (.getPath url))
  (-query [_] (.getDecodedQuery url))
  (-hash [_] (.getFragment url))

  Object
  (toString [_] (str url)))

(extend-protocol IPrintWithWriter
  Url
  (-pr-writer [o writer opts]
    (-write writer (str "#<" `Url " " o ">"))))

(defn resolve [uri & more]
  (reduce #(.resolve %1 %2) uri more))
