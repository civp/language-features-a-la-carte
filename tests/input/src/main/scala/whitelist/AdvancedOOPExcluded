/*
mode = whitelist
excludedFeatures = [
  AdvancedOop
]
*/

transparent trait Baz { /*
^^^^^^^^^^^
not in the allowed features: ADTs, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  def lmnop(y: Int): Int
}

open class Foo { /*
^^^^
not in the allowed features: ADTs, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */

  def abc(x: Float): Boolean = x.isWhole

  final def wxyz(s: String): Int = s.length /*
  ^^^^^
  not in the allowed features: ADTs, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
}

class FooImpl extends Foo, Baz {
  override def abc(x: Float): Boolean = x.isNaN
  override def lmnop(y: Int): Int = 2*y
}

case class Foo2(i: Int)
