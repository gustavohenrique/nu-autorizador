(ns autorizador.core
  (:require [cheshire.core :refer :all]))

; (def account clojure.lang.PersistentHashMap/EMPTY)

(defn create-bank-account
  "Cria uma conta bancaria"
  [account]
  (println "inserindo" account))

(defn -main
  "Autoriza ou rejeita uma transacao"
  ([] (println "é obrigatório passar uma string JSON como argumento."))
  ([data]
    (println "Hello=" data "!")
    (def decoded (parse-string data true))
    (if-let [val (:account decoded)]
      (println "eh conta" val))))
