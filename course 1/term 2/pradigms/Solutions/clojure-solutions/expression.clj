(require '[clojure.math :as math])
(use '[clojure.string :only (join)])

(defn division [arg1 arg2]
  (if (zero? arg2) ##Inf (/ arg1 arg2))
  )



;============FUNCTIONAL PARSER==============
(def constant constantly)
(defn variable [var] (fn [map] (map var)))

(defn makeOp [func]
  (fn [& args]
    (fn [mapVal]
      (if (seq? args)
        (apply func (mapv #(% mapVal) args))
        (func (args mapVal)))
      )
    )
  )
(def add (makeOp +))
(def subtract (makeOp -))
(def divide (makeOp division))
(def multiply (makeOp *))
(def negate (makeOp -))

(def sin (makeOp math/sin))
(def cos (makeOp math/cos))

(def functionalMapOps
  {"+"      add
   "-"      subtract
   "/"      divide
   "*"      multiply
   "negate" negate
   "sin"    sin
   "cos"    cos
   "const"  constant
   "var"    variable}
  )

(defn defTerm [v arg]
  (if (number? arg)
    ((v "const") arg)
    ((v "var") (str arg))
    )
  )
(defn parseBrackets [ops]
  (fn [op & args] (if (not (empty? args))
                    (apply (ops (str op))
                           (map
                             #(if (list? %)
                                (apply (parseBrackets ops) %)
                                (defTerm ops %)
                                ) args)
                           )

                    (defTerm ops op)
                    ))
  )
(defn parse [v]
  (fn [expr]
    (let [e (read-string (str expr)) parser (parseBrackets v)]
      (if (list? e)
        (apply parser e)
        (parser e)))
    )
  )

(def parseFunction (parse functionalMapOps))

;================OBJECT PARSER===============
(defn proto-get
  ([obj key] (proto-get obj key nil))
  ([obj key default]
   (cond
     (contains? obj key) (get obj key)
     (contains? obj :proto) (recur (get obj :proto) key default)
     :else default)))

(defn proto-call [obj key & args]
  (apply (proto-get obj key) obj args))

(defn field
  ([key] (field key nil))
  ([key default] (fn [obj] (proto-get obj key default)))
  )
(defn method [key]
  (fn [obj & args]
    (apply proto-call obj key args)
    ))

(defn constructor [constr proto]
  (fn [& args] (apply constr {:proto proto} args))
  )

(def _val (field :val))

(def _valId (field :valId))
(def _op (field :op))
(def _opString (field :opString))
(def toString (method :toString))
(def toStringPostfix (method :toStringPostfix))
(def evaluate (method :evaluate))



(defn Constant [arg]
  {
   :val             arg
   :toString        (fn [this] (str (_val this)))
   :toStringPostfix (fn [this] (toString this))
   :evaluate        (constantly arg)
   }
  )

(defn Variable [arg]
  {
   :val             arg
   :valId           (get arg 0)
   :toString        (fn [this] (str (_val this)))
   :toStringPostfix (fn [this] (toString this))
   :evaluate        (fn [this v]
                      (v (clojure.string/lower-case (str (_valId this)))))
   })


(defn Operation [this op opString]
  (fn [& args] (assoc this :op op :val args :opString opString)))
(def OpProto
  {
   :toString        (fn [this]
                      (str "(" (_opString this) " " (join " " (map toString (_val this))) ")"))

   :toStringPostfix (fn [this]
                      (str "(" (join " " (map toStringPostfix (_val this))) " " (_opString this) ")"))
   :evaluate        (fn [this v]
                      (do
                        (apply (_op this) (map #(evaluate % v) (_val this)))
                        ))
   })
(def OpBuilder (constructor Operation OpProto))

(def Add (OpBuilder + '+))
(def Subtract (OpBuilder - '-))
(def Divide (OpBuilder division '/))
(def Multiply (OpBuilder * '*))
(def Negate (OpBuilder unchecked-negate 'negate))

(def Exp (OpBuilder math/exp 'exp))
(def Ln (OpBuilder math/log 'ln))
(def objectMapOps
  {
   "+"      Add
   "-"      Subtract
   "/"      Divide
   "*"      Multiply
   "exp"    Exp
   "ln"     Ln
   "negate" Negate
   "const"  Constant
   "var"    Variable
   }
  )

(def parseObject (parse objectMapOps))

;===================COMB================

(defn -return [value tail] {:value value :tail tail})
(def -valid? boolean)
(def -value :value)
(def -tail :tail)

(defn -show [result]
  (if (-valid? result)
    (str "-> " (pr-str (-value result)) " | " (pr-str (apply str (-tail result))))
    "!"))
(defn tabulate [parser inputs]
  (run! (fn [input] (printf "    %-10s %s\n" (pr-str input) (-show (parser input)))) inputs))

(defn _empty [value] (partial -return value))

(defn _char [p]
  (fn [[c & cs]]
    (if (and c (p c)) (-return c cs))))

(defn _map [f]
  (fn [result]
    (if (-valid? result)
      (-return (f (-value result)) (-tail result)))))

(defn _combine [f a b]
  (fn [str]
    (let [ar ((force a) str)]
      (if (-valid? ar)
        ((_map (partial f (-value ar)))
         ((force b) (-tail ar)))))))

(defn _either [a b]
  (fn [str]
    (let [ar ((force a) str)]
      (if (-valid? ar) ar ((force b) str)))))

(defn _parser [parser]
  (fn [input]
    (-value ((_combine (fn [v _] v) parser (_char #{\u0001})) (str input \u0001)))))



(defn +char [chars] (_char (set chars)))
(defn +char-not [chars] (_char (comp not (set chars))))
(defn +map [f parser] (comp (_map f) parser))
(def +ignore (partial +map (constantly 'ignore)))

(defn iconj [coll value]
  (if (= value 'ignore) coll (conj coll value)))

(defn +seq [& parsers]
  (reduce (partial _combine iconj) (_empty []) parsers))

(defn +seqf [f & parsers] (+map (partial apply f) (apply +seq parsers)))

(defn +seqn [n & parsers] (apply +seqf (fn [& vs] (nth vs n)) parsers))

(defn +or [parser & parsers]
  (reduce (partial _either) parser parsers))

(defn +opt [parser]
  (+or parser (_empty nil)))

(defn +star [parser]
  (letfn [(rec [] (+or (+seqf cons parser (delay (rec))) (_empty ())))] (rec)))

(defn +plus [parser] (+seqf cons parser (+star parser)))

(defn +str [parser] (+map (partial apply str) parser))

(def +parser _parser)


(defn +rules [defs]
  (cond
    (empty? defs) ()
    (seq? (first defs)) (let [[[name args body] & tail] defs]
                          (cons
                            {:name name :args args :body body}
                            (+rules tail)))
    :else (let [[name body & tail] defs]
            (cons
              {:name name :args [] :body body :plain true}
              (+rules tail)))))

(defmacro defparser [name & defs]
  (let [rules (+rules defs)
        plain (set (map :name (filter :plain rules)))]
    (letfn [(rule [{name :name, args :args, body :body}] `(~name ~args ~(convert body)))
            (convert [value]
              (cond
                (seq? value) (map convert value)
                (char? value) `(+char ~(str value))
                (contains? plain value) `(~value)
                :else value))]
      `(def ~name (letfn ~(mapv rule rules) (+parser (~(:name (last rules)))))))))

(def *digit (+char "0123456789.,"))

(defn sign [s tail]
  (if (#{\- \+} s)
    (cons s tail)
    tail))
(def *number (+map read-string (+str (+seqf sign (+opt (+char "+-")) (+plus *digit)))))
(def *space (_char #(Character/isWhitespace %)))
(def *ws (+ignore (+star *space)))

(def *all-chars (mapv char (range 32 128)))
(def *letter (+char (filter #(Character/isLetter %) *all-chars)))
(def *opSym (+char (apply str (apply concat (keys objectMapOps)))))

(def *var (+str (+seqf cons (+char "xXyYzZ") (+star (+char "xXyYzZ")))))

(def *op
  (+seqf #(if (contains? objectMapOps %) %)
         (+str (+seqf cons *opSym
                      (+star *opSym)))))

(defn *definer [val]
  (cond
    (number? val) (Constant val)
    (string? val) (Variable val)
    (seq? val) (apply (objectMapOps (last val)) (map *definer (drop-last val)))
    :else val))

(defn *wsSurround [parser]
  (+seqn 0 *ws parser *ws))

(defn *list [value]
  (+seqn 1
         (+char "(")
         (*wsSurround (+seqf cons value (+plus (+seqn 0 (*wsSurround (+or value *op))))))
         (+char ")")))
(def *value
  (+or
    *var
    *number
    (*list (delay *value))))

(def parseObjectPostfix (+parser (+map *definer (+seqn 0 (*wsSurround *value)))))
