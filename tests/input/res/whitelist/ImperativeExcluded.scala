/*
mode = whitelist
excludedFeatures = [
  ImperativeConstructs
]
*/

var x = 0 /*
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
while (x < 1000){ /*
^^^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  println(x)
  x = x + 1 /*
  ^^^^^^^^^
  not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
}
var y = 1 /*
^^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
do { /*
^^
not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  println(y)
  y += 2
} while(y < 500)

def foo(y: Int): Int = {
  if (y < 0) return 0 /*
             ^^^^^^^^
  not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  var k = 2*y /*
  ^^^
  not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  if (k / 5 == k * 5){
    println("Scala!")
    return -42 /*
    ^^^^^^^^^^
    not in the allowed features: ADTs, AdvancedOop, Annotations, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
  }
  if (k % 2 == 0) k else -1
}