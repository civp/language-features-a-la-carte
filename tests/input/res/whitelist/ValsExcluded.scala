/*
mode = whitelist
excludedFeatures = [
  Vals
]
*/

val x = 10 + 2 /* violation: Vals
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Xml */
val str = "Hello world" /* violation: Vals
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Xml */
val p = 1.5 /* violation: Vals
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Xml */
val y: Long = 15 /* violation: Vals
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Xml */
val z: Int = 15 /* violation: Vals
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Xml */
