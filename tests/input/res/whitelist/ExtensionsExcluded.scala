/*
mode = whitelist
excludedFeatures = [
  Extensions
]
*/

extension(self: String){ /*
^^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  def lastIndex: Int = self.size
  def foo(n: Int): String = self.repeat(n)
}