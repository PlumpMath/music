(ns music.core
(:use
    leipzig.melody
    leipzig.scale
    leipzig.canon
    leipzig.live)
  (:require [overtone.live :as overtone]
            [overtone.synth.stringed :as strings])) 

(strings/gen-stringed-synth ektara 1 true)

(defn pick [distort amp {midi :pitch, start :time, length :duration}]
    (let [synth-id (overtone/at start
                     (ektara midi :distort distort :amp amp :gate 1))]
      (overtone/at (+ start length) (overtone/ctl synth-id :gate 0))))

(defmethod play-note :leader [note]
  (pick 5.0 1.0 note))
(defmethod play-note :follower [note]
  (pick 0.3 1.0 note))
(defmethod play-note :bass [note]
  (pick 0.9 0.2 (update-in note [:pitch] #(- % 12))))

(def melody "A simple melody built from durations and pitches."
               ; Row, row, row your boat,
  (->> (phrase [3/3 3/3 2/3 1/3 3/3]
               [ 0 0 4 3 2])
    (then
               ; Gently down the stream,
       (phrase [2/3 1/3 2/3 1/3 6/3]
               [ 2 3 2 3 4]))
    (then
               ; Merrily, merrily, merrily, merrily,
       (phrase (repeat 12 1/3)
               (mapcat (partial repeat 3) [7 4 2 0])))
    (then
               ; Life is but a dream!
       (phrase [2/3 1/3 2/3 1/3 6/3]
               [ 4 3 2 1 0]))
    (where :part (is :leader))))

(def bass "A bass part to accompany the melody."
  (->> (phrase [1 1 1 1/4 1/4 1/4 1/4]
               [0 -3 0 1 13 2 5])
     (where :part (is :bass))
     (times 4)))

(def more-bass "A moreish bass part to accompany the melody."
  (->> (phrase [3 1/2 1/2]
               [-12 -10 -10])
     (where :part (is :bass))
     (times 4)))

(defn row-row
  "Play the tune 'Row, row, row your boat' as a round."
  [speed key]
  (->> melody
    (with bass)
    (with more-bass)   
    (times 2)
    (canon (comp (simple 4)
                 (partial where :part (is :follower))))
    (where :time speed)
    (where :duration speed)
    (where :pitch key)
    play))

(comment
  (row-row (bpm 120) (comp C sharp major))
  (row-row (bpm 90) (comp low B flat minor))
)
