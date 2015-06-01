(ns cljs.node.types.writer-test
  (:require
   [cljs.node.core :refer [slurp spit]]
   [cljs.node.io :as io :refer-macros [with-open]]
   [cljs.node.types.file :as file]
   [cljs.node.types.writer :as writer]
   [cljs.test :refer-macros [deftest is are run-tests]]))

(def lorem-ipsum
  "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris a
  lorem sit amet ipsum imperdiet sodales quis et nulla. Phasellus
  luctus ante non consequat scelerisque. Donec sed nunc vitae turpis
  ultricies pretium. Maecenas faucibus sagittis felis rutrum
  cursus. Donec eget erat vitae ligula dignissim gravida. Ut imperdiet
  feugiat pellentesque. Phasellus turpis mi, semper at ante eu,
  sollicitudin efficitur justo. Nam congue commodo sagittis. Quisque
  dapibus ex metus, et auctor arcu eleifend a. Maecenas sodales
  elementum leo et euismod. Vivamus gravida justo ipsum, vel
  condimentum odio sollicitudin a. Ut vitae porta libero, et mattis
  tellus. Vestibulum elementum suscipit ornare. Praesent sagittis
  varius facilisis. Cras porta, justo et mattis feugiat, arcu ante
  auctor mauris, ut imperdiet odio magna vel dui. Curabitur fermentum
  convallis orci.

  Morbi id placerat nisi. Duis a ligula sollicitudin, semper nisl in,
  condimentum nisi. Morbi ac diam eget nisi feugiat cursus. Sed
  ullamcorper felis semper massa lobortis, nec accumsan felis
  euismod. Etiam vulputate malesuada eleifend. Etiam hendrerit pretium
  felis, vel convallis elit. Vestibulum porta feugiat tempor. Duis
  pulvinar sollicitudin dolor, et consequat metus ornare at. Aenean
  vitae aliquet mauris, sit amet interdum ex. Pellentesque augue purus,
  imperdiet scelerisque consectetur vitae, facilisis nec sem.")

(deftest writer-test
  (try
    (let [tmp-file (io/file (str "/tmp/_tmpfile_" (int (* 10000 (rand)))))]
      (spit tmp-file lorem-ipsum)
      (is (= lorem-ipsum (slurp tmp-file)))
      (file/-delete! tmp-file))
    (catch js/Error e
      (.log js/console (.-message e))
      (.log js/console (.-stack e)))))

(enable-console-print!)
(run-tests)
