(ns autorizador.logic.account)

(defn check-is-initialized [states]
  (if (or (nil? states) (empty? states))
    false
    (some? (->> states (filter #(contains? (get % :account) :available-limit)) first))))

(defn validate-insufficient-limit [states transaction]
  (let [account (last states)
        account-data (:account account)]
    (if (> (:amount transaction) (:available-limit account-data))
      "insufficient-limit"
      nil)))
