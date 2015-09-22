(ns klojery.ui
  (:use (clojure.core))
  (:import (de.uka.ilkd.key.script ScriptAPI)
           (de.uka.ilkd.key.util KeYConstants)
           (de.uka.ilkd.key.proof.io ProofSaver)
           (de.uka.ilkd.key.settings ProofIndependentSettings)
           (java.io File))
  (:gen-class))