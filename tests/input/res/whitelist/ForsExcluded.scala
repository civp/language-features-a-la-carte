/*
mode = whitelist
excludedFeatures = [
  ForExpr
]
*/

def foo(): Unit = {
  for (i <- 0 until 100){ /*
  ^^^  ^^^^^^^^^^^^^^^^
  not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
    for (j <- 0 until 50){ /*
    ^^^  ^^^^^^^^^^^^^^^
    not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
      for (k <- 0 until 80){ /*
      ^^^  ^^^^^^^^^^^^^^^
      not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
        ()
      }
    }
  }
}
