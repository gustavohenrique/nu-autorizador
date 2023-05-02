(ns autorizador.controllers.authorizer-test
  (:require [clojure.test :refer :all]
            [autorizador.controllers.authorizer :refer :all]))


(deftest do-action-test
  (testing "Conta com cartao ativo e 2 transacoes dentro do limite"
    (let [input [
            "{\"account\": {\"active-card\": true, \"available-limit\": 100}}"
            "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}"
            "{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 30, \"time\": \"2019-02-14T11:00:00.000Z\"}}"]
          expected [
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":50},\"violations\":[]}"]
          output (do-action input)]
      (is (= expected output)))
  )

  (testing "Conta com cartao ativo e 2 transacoes fora do limite"
    (let [input [
            "{\"account\": {\"active-card\": true, \"available-limit\": 100}}"
            "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}"
            "{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 200, \"time\": \"2019-02-15T11:00:00.000Z\"}}"
            "{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 30, \"time\": \"2019-02-16T11:00:00.000Z\"}}"]
          expected [
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[\"insufficient-limit\"]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":50},\"violations\":[]}"]
          output (do-action input)]
      (is (= expected output)))
  )

  (testing "Conta com cartao desativado e 2 transacoes dentro do limite"
    (let [input [
            "{\"account\": {\"active-card\": false, \"available-limit\": 100}}"
            "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}"
            "{\"transaction\": {\"merchant\": \"Habbib's\", \"amount\": 30, \"time\": \"2019-02-14T11:00:00.000Z\"}}"]
          expected [
            "{\"account\":{\"active-card\":false,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":false,\"available-limit\":100},\"violations\":[\"card-not-active\"]}"
            "{\"account\":{\"active-card\":false,\"available-limit\":100},\"violations\":[\"card-not-active\"]}"]
          output (do-action input)]
      (is (= expected output)))
  )

  (testing "Nao deve permitir mais de uma conta"
    (let [input [
            "{\"account\": {\"active-card\": true, \"available-limit\": 100}}"
            "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}"
            "{\"account\": {\"active-card\": true, \"available-limit\": 200}}"]
          expected [
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[\"account-already-initialized\"]}"]
          output (do-action input)]
      (is (= expected output)))
  )

  (testing "Nao deve processar transacoes quando a conta nao foi inicializada"
    (let [input [
            "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T10:00:00.000Z\"}}"
            "{\"account\": {\"active-card\": true, \"available-limit\": 100}}"
            "{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 20, \"time\": \"2019-02-15T11:00:00.000Z\"}}"]
          expected [
            "{\"account\":{},\"violations\":[\"account-not-initialized\"]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"]
          output (do-action input)]
      (is (= expected output)))
  )

  (testing "Não deve haver mais que 1 transação similar (mesmo valor e comerciante) no intervalo de 2 minutos"
    (let [input [
           "{\"account\": {\"active-card\": true, \"available-limit\": 100}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:00.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"McDonald's \", \"amount\": 10, \"time\": \"2019-02-13T11:00:01.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:02.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 15, \"time\": \"2019-02-13T11:00:03.000Z\"}}"]
          expected [
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":80},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":70},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":70},\"violations\":[\"doubled-transaction\"]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":55},\"violations\":[]}"]
          output (do-action input)]
      (is (= expected output)))
  )

  (testing "Quando uma transação viola mais de uma lógica"
    (let [input [
           "{\"account\": {\"active-card\": true, \"available-limit\": 100}}"
           "{\"transaction\": {\"merchant\": \"McDonald's\", \"amount\": 10, \"time\": \"2019-02-13T11:00:01.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 20, \"time\": \"2019-02-13T11:00:02.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 5, \"time\": \"2019-02-13T11:00:07.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 5, \"time\": \"2019-02-13T11:00:08.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 150, \"time\": \"2019-02-13T11:00:18.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 190, \"time\": \"2019-02-13T11:00:22.000Z\"}}"
           "{\"transaction\": {\"merchant\": \"Burger King\", \"amount\": 15, \"time\": \"2019-02-13T12:00:27.000Z\"}}"]
          expected [
            "{\"account\":{\"active-card\":true,\"available-limit\":100},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":90},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":70},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":65},\"violations\":[]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":65},\"violations\":[\"high-frequency-small-interval\",\"doubled-transaction\"]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":65},\"violations\":[\"insufficient-limit\",\"high-frequency-small-interval\"]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":65},\"violations\":[\"insufficient-limit\",\"high-frequency-small-interval\"]}"
            "{\"account\":{\"active-card\":true,\"available-limit\":50},\"violations\":[]}"]
          output (do-action input)]
      (is (= expected output)))
  )
)
