/*
mode = whitelist
allowedFeatures = [
  LiteralsAndExpressions,
  Vals,
  Nulls
]
*/
val x = null
val y: Int = null
val correct = "null"
val z: String = if (y > 0) "hello" else null
