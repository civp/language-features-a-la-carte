/*
mode = whitelist
excludedFeatures = [
  BasicOop,
  AdvancedOop
]
*/

trait Foo { /*
^^^^^
not in the allowed features: ADTs, Annotations, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  class Bar extends Foo /*
  ^^^^^
  not in the allowed features: ADTs, Annotations, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
}
