(ns autorizador.core
  (:use autorizador.controller))

(defn- not-blank?
  [line]
  (not (clojure.string/blank? line)))

(defn -main []
  (let [lines (doall (take-while not-blank? (repeatedly read-line)))
        output (do-action lines)]
    (doseq [out output]
      (println out))))
