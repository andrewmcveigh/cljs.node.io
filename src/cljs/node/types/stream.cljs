(ns cljs.node.types.stream)

(defprotocol Openable
  (open [_ body]
    "Executes body in an \"Open\" context"))

(defprotocol Closeable
  (close [_]))
