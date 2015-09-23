(ns klojery.user
  (:use clojure.core)
  (:use klojery.core)
  (:use klojery.interact))


(def problem (load-problem testfile))
(def contraposition (-> problem .getProofs first))

(println 
  (with-proof contraposition
    ; first open-goal is selected automatically
    (auto)))
  