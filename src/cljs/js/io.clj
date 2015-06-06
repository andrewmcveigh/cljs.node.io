(ns cljs.js.io
  (:refer-clojure :exclude [with-open]))

(defmacro with-open
  "bindings: [symbol (Openable. x x) Calls `open` on Openable wrapping
  body in a fn with optional opts passed to `open`.
  If Openable is Synchronous, returns the result of `body`. 
  If Asynchronous, returns nil."
  [bindings & [opts & body]]
  (assert (= 2 (count bindings)) "Incorrect with-open bindings")
  (assert `(satisfies? Openable ~(first bindings))
          "Bindings must be Openable")
  `(let [~@bindings]
     (open
      ~(first bindings)
      (fn [& _#]
        (try
          (do
            ~@(if (map? opts) body (cons opts body)))
          (finally
            (when (satisfies? Closeable ~(first bindings))
              (close ~(first bindings))))))
      ~(when (map? opts) opts))))
