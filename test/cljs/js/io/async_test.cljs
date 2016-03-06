(ns cljs.js.io.async-test
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :as async]
   [cljs.js.io :as io]
   [cljs.js.io.node :as node]
   [cljs.test :refer-macros [deftest is run-tests]]))


