(defproject autorizador "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [pjstadig/humane-test-output "0.11.0"]
                 [cheshire "5.11.0"]]
  :main autorizador.diplomat.stdin
  :injections [(require 'pjstadig.humane-test-output)
                     (pjstadig.humane-test-output/activate!)]
  ; :profiles {:test {:plugins [[lein-test-report "0.2.0"]]}}
  ; :injections [(require 'clojure.pprint 'test-report.summary)]
  ; :test-report {:summarizers [(comp clojure.pprint/pprint test-report.summary/summarize)]}
  ; :injections [(require 'clojure.pprint)]
  ; :test-report {:reporters [clojure.pprint/pprint]}
  :repl-options {:init-ns autorizador.diplomat.stdin})
