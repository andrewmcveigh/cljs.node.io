(ns cljs.js.io.node-test
  (:refer-clojure :exclude [with-open])
  (:require
   [clojure.string :as string]
   [cljs.js.io :as io :refer-macros [with-open]]
   [cljs.js.io.node :as node]
   [cljs.test :refer-macros [deftest is are run-tests]]))

(def tools-reader-url
  (-> "https://raw.githubusercontent.com
       /uswitch/tools.reader/master/src/main/cljs/cljs/tools/reader.cljs"
      (string/replace #"[\s\n]+" "")
      (io/as-url)))

(deftest url-reader-test
  (try
    (prn tools-reader-url)
    (with-open [r (io/reader tools-reader-url)]
      {:rdr-fn #(io/LineReader. (io/StringReader. % (count %) 0))}
      (try
       (prn (take 5 (drop 5 (io/line-seq r))))
       (catch js/Error e
         (.log js/console (.-message e))
         (.log js/console (.-stack e))
         )
       )
     )
      (catch js/Error e
         (.log js/console (.-message e))
         (.log js/console (.-stack e))
         )
    )

)

(enable-console-print!)
(run-tests)
