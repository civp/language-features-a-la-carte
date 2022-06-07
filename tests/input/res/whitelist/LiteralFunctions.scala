/*
mode = whitelist
allowedFeatures = [
  LiteralFunctions,
  LiteralsAndExpressions,
  Vals
]
*/

(x: Int) => 2*x
(x: Int, y: String) => if (x > 0) y.toUpperCase else y.toLowerCase
val f: Int => Double = _.toDouble
val g: (Int) => Int = (x) => 2*x
val h: (Double, Double) => Double = Math.hypot(_, _)
val k: (String, String, Boolean) => String = (s1, s2, b) => if (b) s1 else s2
