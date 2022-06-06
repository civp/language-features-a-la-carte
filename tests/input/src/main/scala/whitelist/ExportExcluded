/*
mode = whitelist
excludedFeatures = [
  Exports
]
*/

class Foo(x: Int){
  def getMulBy(y: Int): Int = x*y
}

class Bar(f: Foo){
  export f.getMulBy /*
  ^^^^^^
  not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
}
