(ns cljs.node.io-test
  (:require [cljs.node.io :as io :include-macros true]
            [cljs.node.reader :as r]
            [cljs.test :refer-macros [deftest is are run-tests]]))

(deftest io-test
  (println
   (io/with-open [r (io/reader (io/file "project.clj"))]
     (pr-str (io/line-seq r))))
  
  ;; (prn (io/reader (io/file "project.clj")))
  )

(enable-console-print!)
(run-tests)
