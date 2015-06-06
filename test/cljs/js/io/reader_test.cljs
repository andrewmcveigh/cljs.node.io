;; (ns cljs.js.io.reader-test
;;   (:refer-clojure :exclude [with-open])
;;   (:require
;;    [clojure.string :as string]
;;    [cljs.js.io :as io]
;;    [cljs.js.io.reader :as reader]
;;    [cljs.js.io.stream :refer-macros [with-open]]
;;    [cljs.test :refer-macros [deftest is are run-tests]])
;;   (:import cljs.js.io.node.reader.UrlReader))

;; (def tools-reader-url
;;   (-> "https://raw.githubusercontent.com
;;        /uswitch/tools.reader/master/src/main/cljs/cljs/tools/reader.cljs"
;;       (string/replace #"[\s\n]+" "")))

;; (deftest url-reader-test
;;   ;; (with-open [r (UrlReader. tools-reader-url nil)]
;;   ;;   (prn (reader/read-line r))
;;   ;; )
;;   (prn (reader/read-char (StringReader. "test" 4 0)))

;; )

;; (enable-console-print!)
;; (run-tests)
