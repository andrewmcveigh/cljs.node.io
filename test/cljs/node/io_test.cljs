;; (ns cljs.node.io-test
;;   (:require
;;    [cljs.node.core :refer [line-seq]]
;;    [cljs.node.io :as io :include-macros true]
;;    [cljs.node.types.file :as file]
;;    [cljs.node.types.reader :as r]
;;    [cljs.test :refer-macros [deftest is are run-tests]]))

;; (deftest io-test
;;   (try
;;     (let [resource (io/resource "cljs/node/io.cljs")
;;           file (io/file resource)]
;;       (is resource)
;;       (is file)
;;       (is (file/-exists? file))
;;       (io/with-open [reader (io/reader resource)]
;;         (= "(ns cljs.node.io" (first (line-seq reader)))))
;;     (catch js/Error e
;;       (println (.-message e))
;;       (println (.-stack e)))))

;; (enable-console-print!)
;; (run-tests)
