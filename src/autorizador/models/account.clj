(ns autorizador.models.account
  (:require [schema.core :as s]
            [autorizador.models.transaction :as models.transaction]
            [autorizador.models.currency :as models.currency]))

(def account-skeleton
  {:account {:active-card s/Bool
             :available-limit models.currency/Currency}
   (s/optional-key :violations) [s/Str]
   (s/optional-key :transactions) [models.transaction/Transaction]})

(s/defschema Account account-skeleton)
