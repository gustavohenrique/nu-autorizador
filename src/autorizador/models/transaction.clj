(ns autorizador.models.transaction
  (:require [schema.core :as s]
            [autorizador.models.currency :as models.currency]))

(def transaction-skeleton
  {:merchant s/Str
   :amount models.currency/Currency
   :time s/Str})

(s/defschema Transaction transaction-skeleton)
