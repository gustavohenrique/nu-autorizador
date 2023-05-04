(ns autorizador.models.schemas-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [autorizador.models.account :refer :all]))

(deftest validate-account-model-test
  (testing "Account nao precisa ter as keywords :transactions e :violations"
    (let [account {:account {:available-limit 100 :active-card true}}
          expected (s/validate Account account)]
      (is (= account expected))))

  (testing "Account precisa ter a keyword :accounts"
    (is (thrown? Exception (s/validate Account {:violations [] :transactions []}))))

  (testing "A keyword :available-limit deve ter um numero inteiro positivo ou zero"
    (let [account {:account {:available-limit 100 :active-card true}}
          expected (s/validate Account account)]
      (is (= account expected))
      (is (thrown? Exception (s/validate Account {:available-limit -1 :active-card true})))))
)
