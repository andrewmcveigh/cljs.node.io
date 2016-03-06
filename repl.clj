(set! *print-length* 1)
(require 'cljs.repl)
(require 'cljs.repl.node)

(cemerick.piggieback/cljs-repl (cljs.repl.node/repl-env))
