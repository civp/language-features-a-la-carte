/*
mode = whitelist
excludedFeatures = [
  Annotations
]
*/

@Test /*
^^^^^
not in the allowed features: ADTs, AdvancedOop, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
def foo_test(): Unit = { fail("Not implemented") }

@bar def someFunction(x: Int, y: String): Boolean = (x + y.length) % 2 == 0 /*
^^^^
not in the allowed features: ADTs, AdvancedOop, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */

@Annot1 /*
^^^^^^^
not in the allowed features: ADTs, AdvancedOop, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
@Annot2 /*
^^^^^^^
not in the allowed features: ADTs, AdvancedOop, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
case class XYZ(x: String)

@deprecated("do not use") /*
^^^^^^^^^^^^^^^^^^^^^^^^^
not in the allowed features: ADTs, AdvancedOop, BasicOop, ContextualConstructs, Defs, Exports, Extensions, ForExpr, ImperativeConstructs, Imports, Infixes, Inlines, Laziness, LiteralFunctions, LiteralsAndExpressions, Metaprogramming, Nulls, Packages, PolymorphicTypes, StringInterpolation, Vals, Xml */
def baz = 0