(ns autorizador.logic)

(defn- check-is-account-initialized [states]
  (if (or (nil? states) (empty? states))
    false
    (some? (->> states (filter #(contains? (get % :account) :available-limit)) first))))

(defn get-small-interval-transaction [account transaction]
  (let [prev (:transaction account)
        before (. java.time.Instant parse (:time prev))
        after (. java.time.Instant parse (:time transaction))
        interval (abs (.between java.time.temporal.ChronoUnit/MINUTES before after))]
    (if (<= interval 2)
      account
      nil)))

(defn is-doubled-transaction? [account t2]
  (let [t1 (:transaction account)]
    (and (= (:merchant t1) (:merchant t2))
         (= (:amount t1) (:amount t2)))))

(defn has-doubled-transaction? [states transaction]
  (let [accounts (filter #(contains? % :transaction) states)
        transactions (map #(get-small-interval-transaction % transaction) accounts)
        transactions-from-same-merchant (filter #(is-doubled-transaction? % transaction) transactions)]
    (not-empty (vec (remove nil? transactions-from-same-merchant)))))

(defn has-high-frequency-small-interval? [states transaction]
  (let [accounts (filter (fn [item] (and (contains? item :transaction) (empty? (:violations item)))) states)
        transactions (map #(get-small-interval-transaction % transaction) accounts)]
    (>= (count (remove nil? transactions)) 3)))

(defn validate-insufficient-limit [states transaction]
  (let [account (last states)
        account-data (:account account)]
    (if (> (:amount transaction) (:available-limit account-data))
      "insufficient-limit"
      nil)))

(defn validate-high-frequency-small-interval [states transaction]
  (if (has-high-frequency-small-interval? states transaction)
    "high-frequency-small-interval"
    nil))

(defn validate-doubled-transaction [states transaction]
  (if (has-doubled-transaction? states transaction)
    "doubled-transaction"
    nil))

(defn manage-account
  [states input]
  (let [is-account-operation? (contains? input :account)
        is-transaction-operation? (not is-account-operation?)
        is-account-initialized? (check-is-account-initialized states)
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
            (let [validations (map #(apply % [states transaction]) [validate-insufficient-limit
                                                                    validate-high-frequency-small-interval
                                                                    validate-doubled-transaction])
                  violations (vec (remove nil? validations))]
              (if (empty? violations)
                {:account (update account-data :available-limit - (get transaction :amount)), :violations [], :transaction transaction}
                (assoc account :violations violations :transaction transaction)))
          ))
      :else
        (last states)
      ))) 

(defn process-operations [states operations]
  (loop [states states
         operations operations]
    (if (empty? operations)
      states
      (let [account (manage-account states (first operations))]
        (recur (conj states account) (rest operations))
      )
    ))
  )
