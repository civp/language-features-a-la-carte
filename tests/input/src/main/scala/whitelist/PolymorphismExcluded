/*
mode = whitelist
excludedFeatures = [
  PolymorphicTypes
]
*/

class Foo[T] /*
          ^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, StringInterpolation, Vals, Xml */

trait Baz[U] { /*
          ^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, StringInterpolation, Vals, Xml */
  def abcd(): U
}
case class Bar(x: Int) extends Baz[Int]{ /*
                               ^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, StringInterpolation, Vals, Xml */
  def abcd(): Int = 0
}

def wxyz[SomeType](st: SomeType): SomeType = st /*
         ^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, StringInterpolation, Vals, Xml */

def f() = wxyz[Int](0) /*
          ^^^^^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, StringInterpolation, Vals, Xml */
