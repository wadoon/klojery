


(def a (load-problem "share/MaxAndSum.java"))
a
(def first-obligation (first a))

(start-proof a
             (macro 'autopilot))
