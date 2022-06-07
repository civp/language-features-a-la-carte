/*
mode = whitelist
allowedFeatures = [
  LiteralsAndExpressions,
  Defs,
  ContextualConstructs,
  PolymorphicTypes
]
*/

// taken from Scala 3 language reference
given intOrd: Ord[Int] with
  def compare(x: Int, y: Int) = if x < y then -1 else if x > y then +1 else 0

def foo(using ord: Ord[Int]): Int = ???

def bar(x: Int)(implicit ctx: String): Boolean = ctx.isEmpty
