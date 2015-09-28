(ns klojery.interact
  (:use (clojure.core))
  (:import (de.uka.ilkd.key.clojure ProofScript)
           (de.uka.ilkd.key.logic Term)
           (java.io File))
  (:gen-class))

(def ^:dynamic *current-proof* nil)
(def ^:dynamic *current-goal* nil)
(def ^:dynamic *default-auto-steps* 1000)

;;; utillities 
(defmacro with-proof [proofexpr & forms] 
  "Execute the given forms with activate 'proofexpr`"
  `(with-redefs [*current-proof* ~proofexpr]
     (with-goal (get-first-open-goal)
     ~@forms
     *current-proof*)))

(defmacro with-goal [goalexpr & forms] 
  "Execute the given forms with activate 'proofexpr`"
  `(with-redefs [*current-goal* ~goalexpr]
     ~@forms))

(defn get-first-open-goal
  ([proof]
    (first (.openGoals proof)))
  ([]
    (get-first-open-goal *current-proof*)))
  
(defn open-goals
  ([proof]
    (.openGoals proof))
  ([]
    (.openGoals *current-proof*)))


(defmacro with-all-open-goals 
  ""
  ([proof & forms]
    `(doseq [goal# (open-goals ~proof)]
       (with-goal goal#
         ~@forms))))
;;;

(defn str->term [^String string & {:keys [proof] :or {:proof *current-proof*}}]  
  (ProofScript/toTerm proof string de.uka.ilkd.key.logic.sort.Sort/FORMULA))

(defn term [obj & {:keys [proof] :or {:proof *current-proof*}}]
  (if (instance? Term obj)
    obj
    (str->term (str obj) :proof proof)))

;;; -------------------------------------
(defmacro -ensure-proof
  ([proof & forms]
  `(let [~proof (or ~proof *current-proof*)]
     (if (nil? ~proof)
       (throw (IllegalStateException. "*current-proof* is nil"))
       (do ~@forms)))))

(defmacro -ensure-goal 
  [goal & forms]
  `(let [~goal  (or ~goal  *current-goal*)]
     (if (nil? ~goal)
       (throw (IllegalStateException. "*current-goal* is nil"))
       (do ~@forms))))


(defmacro -ensure-proof-and-goal
  [proof goal & forms]
  `(-ensure-proof ~proof (-ensure-goal ~goal ~@forms)))

(defn macro 
  [name & {:keys [proof goal]}]
  "Applies a macro to the current proof.
Available macros are:

* autopilot
* ..."
  (-ensure-proof-and-goal
    proof goal
    (ProofScript/macro proof goal (str name))))

;;; -------------------------------------
(defn rule
  ""  
  ([name & {:keys [proof goal formula on occ ]}]
    (let [occ (or occ 0)]
      (println "Occ: " occ)
      (-ensure-proof-and-goal 
        proof goal
        (ProofScript/rule proof goal (str name) formula on occ)))))

;;; -------------------------------------
(defn instantiate 
  [{:keys [proof goal with var formula occ hide] :or {:occ -1 :hide false}}]
  (-ensure-proof-and-goal 
    proof goal
    (ProofScript/instantiate proof goal with var formula occ hide)))

;;; --------------------------------------
(defn tryclose [ & {:keys [steps proof goal]}]
  (-ensure-proof-and-goal 
    proof goal 
    (ProofScript/tryclose  proof goal steps)))

;;; --------------------------------------
(defn smt [name & {:keys [steps proof goal]}]
  (-ensure-proof-and-goal 
    proof goal
    (ProofScript/smt  proof goal (str name))))

;;; -------------------------------------
(defn set!
  ""
  ([options & {:keys [proof]}]
    (-ensure-proof proof    
                   (ProofScript/set proof options)))
  
  ;;([key value & {:keys [proof]}]    
  ;;  (ProofScript/set proof (str key) (str value))))
)
;;; --------------------------------------
(defn tryclose [ & {:keys [proof goal steps] 
                    :or {:proof *current-proof* :goal *current-goal* :steps *default-auto-steps*}}]
  (-ensure-proof-and-goal 
    proof goal
    (ProofScript/tryclose proof goal steps)))
   
;;; --------------------------------------
(defn script [filename & {:keys [proof]}]
  (-ensure-proof 
    proof
    (ProofScript/script proof filename)))


;;; --------------------------------------
(defn leave
  ""
  [& {:keys [proof goal ] :or {:proof *current-proof* :goal *current-goal*}}]
  (-ensure-proof-and-goal 
    proof goal
    (ProofScript/leave proof goal)))

;;; --------------------------------------
(defn auto
  [& {:keys [proof goal steps] :or {:steps 1000}}]
  (-ensure-proof-and-goal 
    proof goal
    (ProofScript/auto proof goal) proof)) ;; TODO STEPS

;;; start nrepl
;(use '[clojure.tools.nrepl.server :only (start-server stop-server)])
;(defonce server (start-server :port 7888))
;(println "Server started")
