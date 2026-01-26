(ns squeaky-clean
  (:require [clojure.string :as str]))

(defn in-range
  [c s e]
  (let [l (compare c s)
        r (compare c e)]
    (and (or (zero? l) (pos? l))
         (or (zero? r) (neg? r)))))

(defn char-cleaner
  [c flag]
  (cond
    (= c \space) "_"
    (or (in-range c \u0000 \u001F)
        (in-range c \u007F \u009F)) "CTRL"
    (in-range c \α \ω) ""
    (not (or (= c \_)
             (Character/isLetter c))) ""
    flag (str/upper-case c)
    :else c))

(defn cleaner
  [s & {:keys [flag] :or {flag false}}]
  (lazy-seq
   (let [[c & rest] s
         nflag (= c \-)]
     (if (nil? c)
       nil
       (cons (char-cleaner c flag) (cleaner rest :flag nflag))))))

(defn clean
  "TODO: add docstring"
  [s]
  (str/join
   ""
   (cleaner s)))
