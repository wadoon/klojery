(ns klojery.interact
  (:use (clojure.core))
  (:import (de.uka.ilkd.key.script ScriptAPI)
           (de.uka.ilkd.key.util KeYConstants)
           (de.uka.ilkd.key.proof.io ProofSaver)
           (de.uka.ilkd.key.settings ProofIndependentSettings)
           (java.io File))
  (:gen-class))

(def ^:dynamic *current-proof* nil)
;;;

;;; -------------------------------------
(defmacro macro [name]
  "Applies a macro to the current proof.
Available macros are:

* autopilot
* ..."

  `(.macro *api* '~name))

;;; -------------------------------------
(defmacro rule
  ""
  [name & {:keys [on occ formula]}]
  `(.rule *api* '~name ~on ~occ ~formula))

;;; -------------------------------------
(defmacro instantiate [ & {:keys [var formula occ  with hide]} ]
  `(.instantiate *api* var formula occ with hide))


;;; --------------------------------------
(defmacro tryclose [ & {:keys [steps] :or {:steps nil}}]
  `(.tryclose *api* steps))

;;; --------------------------------------
(defn smt
  [solver]
  (.smt *api* solver)
    []
  (.smt *api* nil))

;;; --------------------------------------
(defmacro set! [name value]
  `(.set *api* (symbol-name '~name) ~value))

;;; --------------------------------------
(defmacro select [formula]
  (.select *api* formula))

;;; --------------------------------------
(defmacro script [file]
  (.script *api* file))

;;; --------------------------------------
(defn leave [] (.leave *api*))

;;; --------------------------------------
(defn exit [] (.exit *api*))

;;; --------------------------------------
(defmacro auto [&{:keys [steps all] :or {:steps nil :all false}}]
  (.auto *api* steps all))

;;; start nrepl
;(use '[clojure.tools.nrepl.server :only (start-server stop-server)])
;(defonce server (start-server :port 7888))
;(println "Server started")
