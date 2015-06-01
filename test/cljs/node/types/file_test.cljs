(ns cljs.node.types.file-test
  (:require
   [cljs.node.io :as io]
   [cljs.node.types.file :as file]
   [cljs.test :refer-macros [deftest is are run-tests]]))

(deftest file-test
  (try
    (let [f (io/file js/__dirname "../../project.clj")
         tmpfile (file/-touch!
                  (io/file "/tmp" (str "_tmpfile_" (int (* 10000 (rand))))))]
     (is (file/-exists? f))
     (is (file/-exists? tmpfile))
     (file/-delete! tmpfile)
     (is (not (file/-exists? tmpfile)))
     (let [new-dir (io/file tmpfile "new")]
       (file/-mkdirs! new-dir)
       (is (file/-exists? new-dir))))
    (catch js/Error e
      (println (.-message e))
      (println (.-stack e))
      )
    )
  
  )

(enable-console-print!)
(run-tests)
