(ns autorizador.models.currency
  (:require [schema.core :as s]))

(defn gt-zero? [n] (>= n 0))
(def Currency (s/constrained s/Num gt-zero?))
