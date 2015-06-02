(ns cljs.node.io.types.url
  (:require [cljs.nodejs :as node]))

(def node-url (node/require "url"))

(defrecord URL [protocol auth host port hostname pathname search query hash])

(defn from-node-url [url-obj]
  (URL. (.-protocol url-obj)
        (.-auth url-obj)
        (.-host url-obj)
        (.-hostname url-obj)
        (.-pathname url-obj)
        (.-search url-obj)
        (.-query url-obj)
        (.-hash url-obj)))
