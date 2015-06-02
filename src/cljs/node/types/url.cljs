(ns cljs.node.io.types.url
  (:require [cljs.nodejs :as node]))

(defrecord URL [protocol auth host port hostname pathname search query hash])
