/*
mode = whitelist
excludedFeatures = [
  ContextualConstructs
]
*/

// taken from Scala 3 language reference
given intOrd: Ord[Int] with /*
^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  def compare(x: Int, y: Int) = if x < y then -1 else if x > y then +1 else 0

def foo(using ord: Ord[Int]): Int = ??? /*
              ^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */

def bar(x: Int)(implicit ctx: String): Boolean = ctx.isEmpty /*
                         ^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
