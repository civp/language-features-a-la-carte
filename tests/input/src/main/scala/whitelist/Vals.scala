/*
mode = whitelist
excludedFeatures = [
  Vals
]
*/
object ValsNotAllowed {
  val x = 10 + 2 /* violation: Vals
  ^^^
  not in the allowed features: Metaprogramming, Extensions, Laziness, Imports, LiteralFunctions, StringInterpolation, LiteralsAndExpressions, ADTs, BasicOopAddition, AdvancedOOPAddition, PolymorphicTypes, AlwaysAllowed, Annotations, Xml, ImperativeConstructs, ForExpr, Infixes, Inlines, Defs, ContextualConstructs, Packages, Exports, ADTs, Nulls, ADTs, BasicOopAddition */
  val str = "Hello world" /* violation: Vals
  ^^^
  not in the allowed features: Metaprogramming, Extensions, Laziness, Imports, LiteralFunctions, StringInterpolation, LiteralsAndExpressions, ADTs, BasicOopAddition, AdvancedOOPAddition, PolymorphicTypes, AlwaysAllowed, Annotations, Xml, ImperativeConstructs, ForExpr, Infixes, Inlines, Defs, ContextualConstructs, Packages, Exports, ADTs, Nulls, ADTs, BasicOopAddition */
  val p = 1.5 /* violation: Vals
  ^^^
  not in the allowed features: Metaprogramming, Extensions, Laziness, Imports, LiteralFunctions, StringInterpolation, LiteralsAndExpressions, ADTs, BasicOopAddition, AdvancedOOPAddition, PolymorphicTypes, AlwaysAllowed, Annotations, Xml, ImperativeConstructs, ForExpr, Infixes, Inlines, Defs, ContextualConstructs, Packages, Exports, ADTs, Nulls, ADTs, BasicOopAddition */
  val y: Long = 15 /* violation: Vals
  ^^^
  not in the allowed features: Metaprogramming, Extensions, Laziness, Imports, LiteralFunctions, StringInterpolation, LiteralsAndExpressions, ADTs, BasicOopAddition, AdvancedOOPAddition, PolymorphicTypes, AlwaysAllowed, Annotations, Xml, ImperativeConstructs, ForExpr, Infixes, Inlines, Defs, ContextualConstructs, Packages, Exports, ADTs, Nulls, ADTs, BasicOopAddition */
  val z: Int = 15 /* violation: Vals
  ^^^
  not in the allowed features: Metaprogramming, Extensions, Laziness, Imports, LiteralFunctions, StringInterpolation, LiteralsAndExpressions, ADTs, BasicOopAddition, AdvancedOOPAddition, PolymorphicTypes, AlwaysAllowed, Annotations, Xml, ImperativeConstructs, ForExpr, Infixes, Inlines, Defs, ContextualConstructs, Packages, Exports, ADTs, Nulls, ADTs, BasicOopAddition */
}
