(ns autorizador.models.schemas-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [autorizador.models.account :refer :all]))

(deftest validate-account-model-test
  (testing "Account nao precisa ter as keywords definidas"
    (is (= {} (s/validate Account {}))))

  (testing "A keyword :available-limit deve ter um numero inteiro positivo ou zero"
    (let [account {:account {:available-limit 100 :active-card true}}
          expected (s/validate Account account)]
      (is (= account expected))
      (is (thrown? Exception (s/validate Account {:available-limit -1 :active-card true})))))
)
