(ns card-games)

(defn rounds
  "Takes the current round number and returns
   a `list` with that round and the _next two_."
  [n]
  (take 3 (iterate inc n)))

(defn concat-rounds
  "Takes two lists and returns a single `list`
   consisting of all the rounds in the first `list`,
   followed by all the rounds in the second `list`"
  [l1 l2]
  (concat l1 l2))

(defn contains-round?
  "Takes a list of rounds played and a round number.
   Returns `true` if the round is in the list, `false` if not."
  [l n]
  (boolean (some #{n} l)))

(defn card-average
  "Returns the average value of a hand"
  [hand]
  (let [ttl (reduce + hand)
        cnt (count hand)]
    (double (/ ttl cnt))))

(defn approx-average?
  "Returns `true` if average is equal to either one of:
  - Take the average of the _first_ and _last_ number in the hand.
  - Using the median (middle card) of the hand."
  [hand]
  (let [avg (card-average hand)
        avg' (card-average [(first hand) (last hand)])
        med (double (nth hand (int (/ (count hand) 2))))]
    (or (= avg avg')
        (= avg med))))

(defn average-even-odd?
  "Returns true if the average of the cards at even indexes
   is the same as the average of the cards at odd indexes."
  [hand]
  (= (card-average (take-nth 2 hand))
     (card-average (take-nth 2 (rest hand)))))

(defn maybe-double-last
  "If the last card is a Jack (11), doubles its value
   before returning the hand."
  [hand]
  (let [vec-hand (vec hand)
        header (pop vec-hand)
        last (peek vec-hand)]
    (concat (list* header) (list (if (= last 11) 22 last)))))
