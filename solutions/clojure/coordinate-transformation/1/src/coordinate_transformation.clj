(ns coordinate-transformation)

(defn translate2d
  "Returns a function making use of a closure to
   perform a repeatable 2d translation of a coordinate pair."
  [dx dy]
  #(vector (+ %1 dx) (+ %2 dy)))

(defn scale2d
  "Returns a function making use of a closure to
   perform a repeatable 2d scale of a coordinate pair."
  [sx sy]
  #(vector (* %1 sx) (* %2 sy)))

(defn compose-transform
  "Create a composition function that returns a function that
   combines two functions to perform a repeatable transformation."
  [f g]
  #(apply g (f % %2)))

(defn memoize-transform
  "Returns a function that memoizes the last result.
   If the arguments are the same as the last call,
   the memoized result is returned."
  [f]
  (let [last-arg (atom nil)
        last-result (atom nil)]
    (fn [& coord]
      (if (= coord @last-arg)
        @last-result
        (do
          (reset! last-arg coord)
          (reset! last-result (apply f coord))
          @last-result)))))
