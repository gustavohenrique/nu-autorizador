(ns autorizador.components.json
  (:require [cheshire.core :refer [parse-string generate-string]]))

(defn str->json [input]
  (try
    (parse-string input true)
    (catch Exception e [e])
  ))

(defn json->str [input]
  (generate-string input))
