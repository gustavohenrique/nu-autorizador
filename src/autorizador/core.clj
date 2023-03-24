(ns autorizador.core
  (:use autorizador.controller)
  (:require [clojure.string :refer [join split]]))


(defn -main
  "Autoriza ou rejeita uma transacao"
  ([] (println "é obrigatório passar uma string JSON como argumento."))
  ([& args]
    (let [param (join "" args)
          lines (split param #"}}")
          json (mapv #(str % "}}") lines)
          output (do-action json)]
      (doseq [out output]
        (println out)))
  ))
