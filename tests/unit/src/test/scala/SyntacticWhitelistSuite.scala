package tests

import syntactic.whitelist.WhitelistChecker
import syntactic.whitelist.Features._
import syntactic.whitelist.Feature

class SyntacticWhitelistSuite extends SyntacticSuite {
  
  private val allFeatures: List[Feature] = List(
    LiteralsAndExpressions,
    Nulls,
    Vals,
    Defs,
    ADTs,
    LiteralFunctions,
    ForExpr,
    PolymorphicTypes,
    Laziness,
    BasicOop,
    AdvancedOop,
    ImperativeConstructs,
    ContextualConstructs,
    Extensions,
    Metaprogramming,
    Packages,
    Imports,
    Exports,
    Xml,
    StringInterpolation,
    Annotations,
    Infixes,
    Inlines
  )

  test("vals-not-allowed") {
    val path = getTestPath("whitelist/Vals")
    val excludedFeatures = List(Vals)
    val allowedFeatures = allFeatures.filter(!excludedFeatures.contains(_))
    val checker = WhitelistChecker(allowedFeatures)
    checkPath(checker, path)
  }

}
