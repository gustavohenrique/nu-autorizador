(ns autorizador.models.account
  (:require [schema.core :as s]
            [autorizador.models.transaction :as models.transaction]
            [autorizador.models.currency :as models.currency]))

(def account-skeleton
  {(s/optional-key :account) {(s/optional-key :active-card) s/Bool
                              (s/optional-key :available-limit) models.currency/Currency}
   (s/optional-key :violations) [s/Str]
   (s/optional-key :transaction) models.transaction/Transaction})

(s/defschema Account account-skeleton)
(def Accounts [Account])
