(ns autorizador.controller
  (:use autorizador.logic)
  (:use autorizador.components))

(defn do-action [json]
  (let [operations (map #(str->json %) json)
        states (process-operations [] operations)
        accounts (map #(dissoc % :transaction) states)]
    (mapv json->str accounts)))
