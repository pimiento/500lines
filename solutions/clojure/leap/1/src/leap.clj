(ns leap)

(defn leap-year?
  "Returns true if the given year is a leap year;
  otherwise, it returns false."
  [year]
  ;; function body
  (let [d100 (zero? (rem year 100))
        d400 (zero? (rem year 400))
        d4 (zero? (rem year 4))]
    (cond
      (and d100 d400) true
      d100 false
      d4 true
      :else false)))
