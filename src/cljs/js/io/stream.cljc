(ns cljs.js.io.stream
  (:refer-clojure :exclude [with-open])
  (:require
   [clojure.walk :as walk]))

(defprotocol Openable
  (open [_ body] [_ body opts] "Executes body in an \"Open\" context"))

(defprotocol Closeable
  (close [_]))

#?(:clj
   (defmacro with-open
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
         ~(when (map? opts) opts)))))

(comment

  (with-open [r (Reader. "tst")]
   (with-open [r1 (Reader2. "testel")]
     (read r)))
  
  
  )
