(ns autorizador.controllers.authorizer
  (:require [schema.core :as s]
            [clojure.string :refer [blank?]]
            [autorizador.logic.account :as a]
            [autorizador.logic.transaction :as t]
            [autorizador.models.account :as models.account])
  (:use autorizador.components.json))

(s/set-fn-validation! true)

(s/defn manage-account :- models.account/Account
  [states :- models.account/Accounts, input :- models.account/Account]
  (let [is-account-operation? (contains? input :account)
        is-transaction-operation? (not is-account-operation?)
        is-account-initialized? (a/check-is-initialized states)
        is-account-not-initialized? (not is-account-initialized?)]
    (cond
      (and is-account-operation? is-account-not-initialized?)
        (assoc input :violations [])
      (and is-account-operation? is-account-initialized?)
        (assoc (first states) :violations ["account-already-initialized"])
      (and is-transaction-operation? is-account-not-initialized?)
        {:account {}, :violations ["account-not-initialized"]}
      (and is-transaction-operation? is-account-initialized?)
        (let [account (last states)
              account-data (:account account)
              transaction (:transaction input)]
          (if (not (:active-card account-data))
            (assoc account :violations ["card-not-active"]) 
            (let [validations (map #(apply % [states transaction])
                                   [a/validate-insufficient-limit
                                    t/validate-high-frequency-small-interval
                                    t/validate-doubled-transaction])
                  violations (vec (remove blank? validations))]
              (if (empty? violations)
                {:account (update account-data :available-limit - (get transaction :amount)), :violations [], :transaction transaction}
                (assoc account :violations violations :transaction transaction)))
          ))
      :else
        (last states)
      ))) 

(defn- process-operations [states operations]
  (loop [states states
         operations operations]
    (if (empty? operations)
      states
      (let [account (manage-account states (first operations))]
        (recur (conj states account) (rest operations))
      )
    )))

(defn do-action [json]
  ; TODO: Pensar em como usar adapter para converter para models
  (let [operations (map #(str->json %) json)
        states (process-operations [] operations)
        accounts (map #(dissoc % :transaction) states)]
    (mapv json->str accounts)))
