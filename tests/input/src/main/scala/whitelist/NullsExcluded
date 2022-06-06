/*
mode = whitelist
excludedFeatures = [
  Nulls
]
*/
val x = null /*
        ^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
val y: Int = null /*
             ^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
val correct = "null"
val z: String = if (y > 0) "hello" else null /*
                                        ^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
