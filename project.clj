(defproject klojery "0.1.0-SNAPSHOT"
  :description "Clojure+Key=KlojErY for a better proof environment"
  :url "http://github.com/wadoon/KlojErY"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]

  ;; does not work, weigl
  ;;:plugin [[lein-localrepo "0.5.3"]]

  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; better repl (default included in leiningen)
                 [reply "0.3.7"]

                 ;; Key Stuff
                 [org.key-project/key.core 2.5]
                 [org.key-project/key.core.proof_references 2.5]
                 [org.key-project/key.core 2.5]
                 [org.key-project/key.core.symbolic_execution 2.5]
                 [org.key-project/key.removegenerics 2.5]
                 [org.key-project/key.ui 2.5]
                 [org.key-project/key.util 2.5]
                 [org.key-project/antlr 2.5]
                 [org.key-project/recoderKey 2.5]]

  :main ^:skip-aot klojery.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
