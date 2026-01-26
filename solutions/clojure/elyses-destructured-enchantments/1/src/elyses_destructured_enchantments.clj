(ns elyses-destructured-enchantments)

(defn first-card
  "Returns the first card from deck."
  [deck]
  (let [[f] deck]
    f))

(defn second-card
  "Returns the second card from deck."
  [deck]
  (let [[_ s] deck]
    s))

(defn swap-top-two-cards
  "Returns the deck with first two items reversed."
  [deck]
  (let [[f s & rest] deck]
    (into [s f] rest)))

(defn discard-top-card
  "Returns a sequence containing the first card and
   a sequence of the remaining cards in the deck."
  [deck]
  (let [[f & rest] deck]
    (conj [f] (vec rest))))

(def face-cards
  ["jack" "queen" "king"])

(defn insert-face-cards
  "Returns the deck with face cards between its head and tail."
  [deck]
  (let [[f & rest] deck]
    (into (into [f] face-cards) rest)))
