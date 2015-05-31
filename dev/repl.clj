(ns repl
  (:require
   [cemerick.piggieback]
   [cljs.build.api]
   [cljs.closure]
   [cljs.repl]
   [cljs.repl.node]))

(defn watch []
  (cljs.build.api/watch "src"
                        {:main 'cljs.node.io
                         :output-to "out/main.js"}))

(defn build []
  (cljs.closure/build "src"
                      {:main 'cljs.node.io
                       :output-to "out/main.js"
                       :verbose true}))

(defn nrepl []
  (cemerick.piggieback/cljs-repl (cljs.repl.node/repl-env)
                                 ;; :watch "src"
                           ;;      :output-dir "out"
                                 ))

;; (cljs.repl/repl (cljs.repl.node/repl-env)
;;                 ;; :watch "dev"
;;                 ;; :output-dir "out"
;;                 )

;; (cljs.core/enable-console-print!)


(comment

  (nrepl)
  
  )
