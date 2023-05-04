(ns autorizador.logic.account
  (:require [schema.core :as s]
            [autorizador.models.account :as models.account]
            [autorizador.models.transaction :as models.transaction]))

(s/defn check-is-initialized :- s/Bool
  [states :- models.account/Accounts]
  (if (or (nil? states) (empty? states))
    false
    (some? (->> states (filter #(contains? (get % :account) :available-limit)) first))))

(s/defn validate-insufficient-limit :- s/Str
  [states :- models.account/Accounts, transaction :- models.transaction/Transaction]
  (let [account (last states)
        account-data (:account account)]
    (if (> (:amount transaction) (:available-limit account-data))
      "insufficient-limit"
      "")))
