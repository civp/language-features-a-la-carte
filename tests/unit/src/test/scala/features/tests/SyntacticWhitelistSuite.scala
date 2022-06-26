package features.tests

import features.syntactic.whitelist.WhitelistChecker
import features.syntactic.whitelist.PredefFeatures._
import features.syntactic.whitelist.Feature

import scala.meta.dialects.{Sbt1, Scala213, Scala3}
import scala.meta.Dialect

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
  
  private def checkWithFeaturesExcluded(path: String, excludedFeatures: List[Feature], dialect: Dialect = Scala3) = {
    val file = getTestFile(path)
    val allowedFeatures = allFeatures.filter(!excludedFeatures.contains(_))
    val checker = WhitelistChecker(allowedFeatures)
    checkFile(checker, file, dialect)
  }

  private def checkWithFeaturesAllowed(path: String, allowedFeatures: List[Feature], dialect: Dialect = Scala3) = {
    val file = getTestFile(path)
    val checker = WhitelistChecker(allowedFeatures)
    checkFile(checker, file, dialect)
  }

  test("literals-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions)
    checkWithFeaturesAllowed("whitelist/Literals", allowedFeatures, Sbt1)
  }

  test("literals-and-expressions-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions)
    checkWithFeaturesAllowed("whitelist/LiteralsAndExpressions", allowedFeatures, Sbt1)
  }

  test("vals-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Vals)
    checkWithFeaturesAllowed("whitelist/Vals", allowedFeatures, Sbt1)
  }

  test("vals-excluded") {
    val excludedFeatures = List(Vals)
    checkWithFeaturesExcluded("whitelist/ValsExcluded", excludedFeatures, Sbt1)
  }

  test("advanced-oop-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, AdvancedOop, Defs)
    checkWithFeaturesAllowed("whitelist/AdvancedOOP", allowedFeatures)
  }

  test("advanced-oop-excluded") {
    val excludedFeatures = List(AdvancedOop)
    checkWithFeaturesExcluded("whitelist/AdvancedOOPExcluded", excludedFeatures)
  }

  test("defs-excluded") {
    val excludedFeatures = List(Defs)
    checkWithFeaturesExcluded("whitelist/DefsExcluded", excludedFeatures)
  }

  test("adts-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, ADTs)
    checkWithFeaturesAllowed("whitelist/ADTs", allowedFeatures)
  }

  // ignored because of automatically generated `def this`
  // can't be marked in the input file
  test("adts-excluded".ignore) {
    val excludedFeatures = List(ADTs, BasicOop, AdvancedOop)
    checkWithFeaturesExcluded("whitelist/ADTsExcluded", excludedFeatures)
  }

  test("not-so-adt-allowed") {
    val allowedFeatures = List(BasicOop)
    checkWithFeaturesAllowed("whitelist/NotSoADT", allowedFeatures)
  }

  test("not-so-adt-excluded") {
    val excludedFeatures = List(BasicOop, AdvancedOop)
    checkWithFeaturesExcluded("whitelist/NotSoADTExcluded", excludedFeatures)
  }

  test("literal-functions-allowed") {
    val allowedFeatures = List(LiteralFunctions, LiteralsAndExpressions, Vals)
    checkWithFeaturesAllowed("whitelist/LiteralFunctions", allowedFeatures, Sbt1)
  }

  test("literal-functions-excluded") {
    val excludedFeatures = List(LiteralFunctions)
    checkWithFeaturesExcluded("whitelist/LiteralFunctionsExcluded", excludedFeatures, Sbt1)
  }

  test("fors-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, ForExpr)
    checkWithFeaturesAllowed("whitelist/Fors", allowedFeatures)
  }

  test("fors-excluded") {
    val excludedFeatures = List(ForExpr)
    checkWithFeaturesExcluded("whitelist/ForsExcluded", excludedFeatures)
  }

  test("import-allowed") {
    val allowedFeatures = List(Imports)
    checkWithFeaturesAllowed("whitelist/Import", allowedFeatures)
  }

  test("import-excluded") {
    val excludedFeatures = List(Imports)
    checkWithFeaturesExcluded("whitelist/ImportExcluded", excludedFeatures)
  }

  test("packages-allowed") {
    val allowedFeatures = List(Packages)
    checkWithFeaturesAllowed("whitelist/Package", allowedFeatures)
  }

  test("packages-excluded") {
    val excludedFeatures = List(Packages)
    checkWithFeaturesExcluded("whitelist/PackageExcluded", excludedFeatures)
  }

  test("nulls-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Vals, Nulls)
    checkWithFeaturesAllowed("whitelist/Nulls", allowedFeatures)
  }

  test("nulls-excluded") {
    val excludedFeatures = List(Nulls)
    checkWithFeaturesExcluded("whitelist/NullsExcluded", excludedFeatures)
  }

  test("polymorphism-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, BasicOop, PolymorphicTypes)
    checkWithFeaturesAllowed("whitelist/Polymorphism", allowedFeatures)
  }

  test("polymorphism-excluded") {
    val excludedFeatures = List(PolymorphicTypes)
    checkWithFeaturesExcluded("whitelist/PolymorphismExcluded", excludedFeatures)
  }

  test("laziness-allowed") {
    val allowedFeatures = List(Vals, Defs, LiteralsAndExpressions, Laziness)
    checkWithFeaturesAllowed("whitelist/Laziness", allowedFeatures)
  }

  test("laziness-excluded") {
    val excludedFeatures = List(Laziness)
    checkWithFeaturesExcluded("whitelist/LazinessExcluded", excludedFeatures)
  }

  test("imperative-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, ImperativeConstructs)
    checkWithFeaturesAllowed("whitelist/Imperative", allowedFeatures, Sbt1)
  }

  test("imperative-excluded") {
    val excludedFeatures = List(ImperativeConstructs)
    checkWithFeaturesExcluded("whitelist/ImperativeExcluded", excludedFeatures, Sbt1)
  }

  test("contextual-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, ContextualConstructs, PolymorphicTypes)
    checkWithFeaturesAllowed("whitelist/Contextual", allowedFeatures)
  }

  test("contextual-excluded") {
    val excludedFeatures = List(ContextualConstructs)
    checkWithFeaturesExcluded("whitelist/ContextualExcluded", excludedFeatures)
  }

  test("extensions-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, Extensions)
    checkWithFeaturesAllowed("whitelist/Extensions", allowedFeatures)
  }

  test("extensions-excluded") {
    val excludedFeatures = List(Extensions)
    checkWithFeaturesExcluded("whitelist/ExtensionsExcluded", excludedFeatures)
  }

  test("metaprogramming-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, ContextualConstructs, PolymorphicTypes, Metaprogramming)
    checkWithFeaturesAllowed("whitelist/Metaprogramming", allowedFeatures)
  }

  test("metaprogramming-excluded") {
    val excludedFeatures = List(Metaprogramming)
    checkWithFeaturesExcluded("whitelist/MetaprogrammingExcluded", excludedFeatures)
  }

  test("exports-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, BasicOop, Defs, Exports)
    checkWithFeaturesAllowed("whitelist/Export", allowedFeatures)
  }

  test("exports-excluded") {
    val excludedFeatures = List(Exports)
    checkWithFeaturesExcluded("whitelist/ExportExcluded", excludedFeatures)
  }

  test("xml-allowed") {
    val allowedFeatures = List(Vals, Xml)
    checkWithFeaturesAllowed("whitelist/Xml", allowedFeatures)
  }

  test("xml-excluded") {
    val excludedFeatures = List(Xml)
    checkWithFeaturesExcluded("whitelist/XmlExcluded", excludedFeatures)
  }

  test("string-interpolation-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Vals, StringInterpolation)
    checkWithFeaturesAllowed("whitelist/StringInterpolation", allowedFeatures)
  }

  test("string-interpolation-excluded") {
    val excludedFeatures = List(StringInterpolation)
    checkWithFeaturesExcluded("whitelist/StringInterpolationExcluded", excludedFeatures)
  }

  test("annotations-allowed") {
    val allowedFeatures = List(LiteralsAndExpressions, Defs, BasicOop, Annotations)
    checkWithFeaturesAllowed("whitelist/Annotations", allowedFeatures)
  }

  test("annotations-excluded") {
    val excludedFeatures = List(Annotations)
    checkWithFeaturesExcluded("whitelist/AnnotationsExcluded", excludedFeatures)
  }

  test("infixes-allowed") {
    val allowedFeatures = List(Defs, LiteralsAndExpressions, Infixes)
    checkWithFeaturesAllowed("whitelist/Infixes", allowedFeatures)
  }

  test("infixes-excluded") {
    val excludedFeatures = List(Infixes)
    checkWithFeaturesExcluded("whitelist/InfixesExcluded", excludedFeatures)
  }

  test("inlines-allowed") {
    val allowedFeatures = List(Defs, LiteralsAndExpressions, Inlines)
    checkWithFeaturesAllowed("whitelist/Inlines", allowedFeatures)
  }

  test("inlines-excluded") {
    val excludedFeatures = List(Inlines)
    checkWithFeaturesExcluded("whitelist/InlinesExcluded", excludedFeatures)
  }

  test("classes-without-braces") {
    val allowedFeatures = List(LiteralsAndExpressions, BasicOop, Defs)
    checkWithFeaturesAllowed("whitelist/NoBraces", allowedFeatures)
  }

}
