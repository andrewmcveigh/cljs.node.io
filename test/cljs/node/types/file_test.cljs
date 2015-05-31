(ns cljs.node.types.file-test
  (:require [cljs.node.types.file :as file]
            [cljs.test :refer-macros [deftest is are run-tests]]))

(deftest file-test
  (let [f (file/file js/__dirname "../../project.clj")
        tmpfile (file/-touch!
                 (file/file "/tmp" (str "_tmpfile_" (int (* 10000 (rand))))))]
    (is (file/-exists? f))
    (is (file/-exists? tmpfile))
    (file/-delete! tmpfile)
    (is (not (file/-exists? tmpfile)))
    (let [new-dir (file/file tmpfile "new")]
      (file/-mkdirs! new-dir)
      (prn new-dir (file/-exists? new-dir))
      )
    )
  
  )

(enable-console-print!)
(run-tests)
