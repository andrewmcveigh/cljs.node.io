(defproject com.andrewmcveigh/cljs.node.io "0.1.0-SNAPSHOT"
  :description "A polymorphic I/O utility functions library for Clojurescript"
  :url "https://github.com/andrewmcveigh/cljs.node.io"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [org.clojure/tools.reader "0.9.3-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.0.6"]]
  :cljsbuild {:test-commands {"test" ["node" :runner "target/test/tools.reader.test.js"]}
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:output-to "out/main.js"
                                   :output-dir "out"
                                   :optimizations :simple
                                   :pretty-print true}}
                       {:id "test"
                        :source-paths ["src" "test"]
                        :notify-command ["node" "target/test/tools.reader.test.js"]
                        :compiler
                        {:output-to  "target/test/tools.reader.test.js"
                         :source-map "target/test/tools.reader.test.js.map"
                         :output-dir "target/test/out"
                         :optimizations :simple}}]}
  :min-lein-version "2.0.0")
