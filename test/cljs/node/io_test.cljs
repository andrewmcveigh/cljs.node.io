(ns cljs.node.io-test
  (:require [cljs.node.io :as io :include-macros true]
            [cljs.node.reader :as r]
            [cljs.test :refer-macros [deftest is are run-tests]]))

(deftest io-test
  (println
   (io/with-open [r (r/sync-file-reader "project.clj")]
     (pr-str (io/line-seq r)))))

(enable-console-print!)
(run-tests)
