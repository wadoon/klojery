(ns klojery.core
  (:use (clojure.core))
  (:import (de.uka.ilkd.key.clojure Core)
           (de.uka.ilkd.key.proof Proof)
           (de.uka.ilkd.key.proof.init AbstractProfile)
           (de.uka.ilkd.key.util KeYConstants)
           (de.uka.ilkd.key.proof.io ProofSaver ProblemLoader)
           (de.uka.ilkd.key.settings ProofIndependentSettings)
           (java.io File))
  (:use klojery.interact)
  (:gen-class))

(defn get-key-version []
  "returns the current version of the used KeY version"
  (KeYConstants/VERSION))

(defn get-key-copyright []
  "returns the copyright note from KeY"
  (KeYConstants/COPYRIGHT))

(defn load-problem [^String filename]
  ""
  (Core/loadProblem filename))


(defn save-proof [^Proof proof ^String filename]
  (.save (ProofSaver. proof (File. filename))))

;;(defn load-proof [^String filename]
;;  (.load (ProofLoader. (File. filename))))


(def testfile
  "/home/weigl/work/key/key/key.ui/examples/standard_key/prop_log/contraposition.key")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (get-key-version) "\n"
           (get-key-copyright) "\n")


  ;(clojure.main/repl :init (fn [] (in-ns 'klojery.core)))
  ;(reply.main/launch args)
  (load-file "usecase.clj"))
