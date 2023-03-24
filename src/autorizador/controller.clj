(ns autorizador.controller
  (:use autorizador.logic)
  (:require [cheshire.core :refer [parse-string generate-string]]))

(defn- parse [input]
  (try
    (parse-string input true)
    (catch Exception e [e])
  ))

(defn- print-json [input]
  (println (generate-string input)))

(defn do-action [json]
  (let [operations (map #(parse %) json)
        states (process-operations [] operations)
        accounts (map #(dissoc % :transaction) states)]
    (mapv generate-string accounts)))
