(ns cljs.node.io
  (:refer-clojure :exclude [with-open]))

(defmacro with-open [bindings & body]
  (assert (= 2 (count bindings)) "Incorrect with-open bindings")
  (assert `(satisfies? cljs.node.reader.Openable ~(first bindings))
          "Bindings must be Openable")
  `(let [~@bindings]
     (cljs.node.reader/open
      ~(first bindings)
      (fn [& _#]
        (try
          (do ~@body)
          (finally
            (when (satisfies? cljs.node.reader.Closeable ~(first bindings))
              (cljs.node.reader/close ~(first bindings)))))))))
