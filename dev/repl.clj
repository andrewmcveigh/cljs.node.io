(ns repl
  (:require
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

#_(defn nrepl []
  (cemerick.piggieback/cljs-repl (cljs.repl.node/repl-env)
                                 ;; :watch "src"
                           ;;      :output-dir "out"
                                 ))
(defn repl []
  (cljs.repl/repl (cljs.repl.node/repl-env)
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

;; (.end (.request (js/require "http") "http://google.com" (fn [x] (.log js/console x) (.on x "data" (fn [data] (.log js/console data))))))
