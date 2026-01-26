(ns log-levels
  (:require [clojure.string :as str]))

(defn split-logmsg
  [s n]
  (nth (map str/trim (str/split s #":" 2)) n))

(defn message
  "Takes a string representing a log line
   and returns its message with whitespace trimmed."
  [s]
  (split-logmsg s 1))

(defn log-level
  "Takes a string representing a log line
   and returns its level in lower-case."
  [s]
  (str/lower-case (str/escape (split-logmsg s 0) {\[ "" \] ""})))

(defn reformat
  "Takes a string representing a log line and formats it
   with the message first and the log level in parentheses."
  [s]
  (let [m (message s) l (log-level s)]
    (str m " (" l ")")))
