/*
mode = whitelist
excludedFeatures = [
  StringInterpolation
]
*/

val version = 3
val langName = s"Scala $version" /*
               ^^^^^^^^^^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, Vals, Xml */
val hello = s"Hello ${langName}" /*
            ^^^^^^^^^^^^^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, Vals, Xml */
val nested = "nested"
val nestedStringInterpolate = s"This ${s"is ${s"a $nested"} interpolation"}" /*
                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, Vals, Xml */
