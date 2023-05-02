(ns autorizador.logic.transaction)

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

(defn validate-high-frequency-small-interval [states transaction]
  (if (has-high-frequency-small-interval? states transaction)
    "high-frequency-small-interval"
    nil))

(defn validate-doubled-transaction [states transaction]
  (if (has-doubled-transaction? states transaction)
    "doubled-transaction"
    nil))
