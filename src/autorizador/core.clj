(ns autorizador.core
  (:use autorizador.controller))

(defn -main []
  (let [lines (for [line (line-seq (java.io.BufferedReader. *in*))] line)
        output (do-action lines)]
    (doseq [out output]
      (println out))))
