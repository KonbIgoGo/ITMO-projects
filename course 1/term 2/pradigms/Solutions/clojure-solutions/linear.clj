(defn makeOp [op vect1 vect2] (mapv op vect1 vect2))
(defn v+ [vect1 vect2] (makeOp + vect1 vect2))
(defn v- [vect1 vect2] (makeOp - vect1 vect2))
(defn v* [vect1 vect2] (makeOp * vect1 vect2))
(defn vd [vect1 vect2] (makeOp / vect1 vect2))
(defn v*s [vect scalar] (makeOp * vect (iterate identity scalar)))

(defn scalar [vect1 vect2] (apply + (v* vect1 vect2)))

(defn vect [vect1 vect2]
  (
    vector
    (- (* (nth vect1 1) (nth vect2 2)) (* (nth vect1 2) (nth vect2 1)))
    (- (* (nth vect1 2) (nth vect2 0)) (* (nth vect1 0) (nth vect2 2)))
    (- (* (nth vect1 0) (nth vect2 1)) (* (nth vect1 1) (nth vect2 0)))
    ))

(defn transpose [matr] (apply mapv vector matr))
(defn m+ [matr1 matr2] (makeOp v+ matr1 matr2))
(defn m- [matr1 matr2] (makeOp v- matr1 matr2))
(defn m* [matr1 matr2] (makeOp v* matr1 matr2))
(defn md [matr1 matr2] (makeOp vd matr1 matr2))
(defn m*s [matr scalar] (makeOp v* matr, (iterate identity (iterate identity scalar))))
(defn m*v [matr vect] (makeOp scalar matr (iterate identity vect)))
(defn m*m [matr1 matr2] (transpose (mapv (partial m*v matr1) (transpose matr2))))

(defn c+ [c1 c2] (makeOp m+ c1 c2))
(defn c- [c1 c2] (makeOp m- c1 c2))
(defn c* [c1 c2] (makeOp m* c1 c2))
(defn cd [c1 c2] (makeOp md c1 c2))
