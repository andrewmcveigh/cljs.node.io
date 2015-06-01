(ns cljs.node.types.writer-test
  (:require
   [cljs.node.core :refer [spit]]
   [cljs.node.io :as io :refer-macros [with-open]]
   [cljs.node.types.writer :as writer]
   [cljs.test :refer-macros [deftest is are run-tests]]))

(deftest writer-test
  (try
    (let [tmp-file (io/file (str "/tmp/_tmpfile_" (int (* 10000 (rand)))))]
     ;; with-open [w (writer/sync-file-writer tmp-file)]
     (spit tmp-file "test")
    
      )
    (catch js/Error e
      (.log js/console (.-message e))
      (.log js/console (.-stack e))
      )
    )
  )

(enable-console-print!)
(run-tests)

