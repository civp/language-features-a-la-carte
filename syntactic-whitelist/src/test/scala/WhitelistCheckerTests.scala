import Features._
import TestRunner.createTest
import org.junit.{AfterClass, Test}

import scala.meta.dialects

class WhitelistCheckerTests {
  import WhitelistCheckerTests._

  @Test def allowLiteralsAndExpressions_should_allow_literals(): Unit = {
    createTest(testController)
      .onFile("LiteralsOnly")
      .withFeatures(AllowLiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectValid()
  }

  @Test def allowLiteralsAndExpressions_should_allow_literals_and_expressions(): Unit = {
    createTest(testController)
      .onFile("LiteralsAndExpressions")
      .withFeatures(AllowLiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectValid()
  }

  @Test def vals_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Vals")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowVals)
      .expectInvalid(2 -> 1, 3 -> 1, 4 -> 1, 5 -> 1, 6 -> 1)
  }

  @Test def vals_should_be_accepted_when_allowed(): Unit = {
    createTest(testController)
      .onFile("Vals")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowVals
      )
      .expectValid()
  }

  @Test def defs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Defs")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowDefs)
      .expectInvalid(2 -> 1, 3 -> 1)
  }

  @Test def allowADTs_should_allow_ADTs(): Unit = {
    createTest(testController)
      .onFile("ADTs")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowADTs
      )
      .expectValid()
  }

  @Test def ADTs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("ADTs")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(
        AllowADTs,
        AllowBasicOop,
        AllowAdvancedOop
      )
      .expectInvalid(2 -> 2, 4 -> 1, 5 -> 3, 6 -> 3,
        3 -> 1  // FIXME weird behavior of Scalameta on a trait without body
      )
  }

  @Test def AllowADTs_should_not_allow_sealed_trait_with_non_case_class(): Unit = {
    createTest(testController)
      .onFile("NotSoADT")
      .withFeatures(AllowADTs)
      .expectInvalid(2 -> 1, 3 -> 1)
  }

  @Test def allowBasicOop_should_allow_sealed_trait_with_non_case_class(): Unit = {
    createTest(testController)
      .onFile("NotSoADT")
      .withFeatures(AllowBasicOop)
      .expectValid()
  }

  @Test def allowLiteralFunctions_should_allow_literal_functions(): Unit = {
    createTest(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(
        AllowLiteralFunctions,
        AllowLiteralsAndExpressions,
        AllowVals
      )
      .expectValid()
  }

  @Test def literal_functions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowLiteralFunctions)
      .expectInvalid(2 -> 1, 3 -> 1, 4 -> 2, 5 -> 1, 6 -> 3, 7 -> 1)
  }

  @Test def allowForExpr_should_allow_for_expressions(): Unit = {
    createTest(testController)
      .onFile("Fors")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowForExpr
      )
      .expectValid()
  }

  @Test def for_expressions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Fors")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowForExpr)
      .expectInvalid(3 -> 2, 4 -> 2, 5 -> 2)
  }

  @Test def AllowImports_should_allow_imports(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withFeatures(AllowImports)
      .expectValid()
  }

  @Test def imports_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowImports)
      .expectInvalid(1 -> 1)
  }

  @Test def AllowPackages_should_allow_packages(): Unit = {
    createTest(testController)
      .onFile("PackageOnly")
      .withFeatures(AllowPackages)
      .expectValid()
  }

  @Test def AllowNull_should_allow_null(): Unit = {
    createTest(testController)
      .onFile("Nulls")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowVals,
        AllowNull
      )
      .expectValid()
  }

  @Test def nulls_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Nulls")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowNull)
      .expectInvalid(1 -> 1, 2 -> 1, 4 -> 1)
  }

  @Test def AllowPolymorphicTypes_should_allow_polymorphic_types(): Unit = {
    createTest(testController)
      .onFile("Polymorphism")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowBasicOop,
        AllowPolymorphicTypes
      )
      .expectValid()
  }

  @Test def polymorphic_types_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Polymorphism")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowPolymorphicTypes)
      .expectInvalid(2 -> 2, 4 -> 2, 7 -> 1, 11 -> 2)
  }

  @Test def AllowLaziness_should_allow_lazy_vals_and_args(): Unit = {
    createTest(testController)
      .onFile("Laziness")
      .withFeatures(
        AllowVals,
        AllowDefs,
        AllowLiteralsAndExpressions,
        AllowLaziness
      )
      .expectValid()
  }

  @Test def laziness_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Laziness")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowLaziness)
      .expectInvalidAtLines(2, 3)
  }

  @Test def AllowAdvancedOop_should_allow_advanced_oop_constructs(): Unit = {
    createTest(testController)
      .onFile("AdvancedOOP")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowAdvancedOop,
        AllowDefs
      )
      .expectValid()
  }

  @Test def advanced_oop_constructs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("AdvancedOOP")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowAdvancedOop)
      .expectInvalidAtLines(2, 6, 10)
  }
  
  @Test def AllowImperativeConstructs_should_allow_imperative_constructs(): Unit = {
    createTest(testController)
      .onFile("Imperative")
      .withDialect(dialects.Sbt1)
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowImperativeConstructs
      )
      .expectValid()
  }

  @Test def imperative_constructs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Imperative")
      .withDialect(dialects.Sbt1)
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowImperativeConstructs)
      .expectInvalidAtLines(2, 3, 5, 7, 8, 14, 15, 18)
  }

  @Test def AllowContextualConstructs_should_allow_contextual_constructs(): Unit = {
    createTest(testController)
      .onFile("Contextual")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowContextualConstructs,
        AllowPolymorphicTypes
      )
      .expectValid()
  }

  @Test def contextual_constructs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Contextual")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowContextualConstructs)
      .expectInvalidAtLines(3, 6, 8)
  }

  @Test def AllowExtensions_should_allow_extensions(): Unit = {
    createTest(testController)
      .onFile("Extensions")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowExtensions
      )
      .expectValid()
  }

  @Test def extensions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Extensions")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowExtensions)
      .expectInvalidAtLines(2)
  }

  @Test def AllowMetaprogramming_should_allow_macros(): Unit = {
    createTest(testController)
      .onFile("Metaprogramming")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowContextualConstructs,
        AllowPolymorphicTypes,
        AllowMetaprogramming
      )
      .expectValid()
  }

  @Test def metaprogramming_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Metaprogramming")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowMetaprogramming)
      .expectInvalid(5 -> 3)
  }

  @Test def AllowExports_should_allow_exports(): Unit = {
    createTest(testController)
      .onFile("Export")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowBasicOop,
        AllowDefs,
        AllowExports
      )
      .expectValid()
  }

  @Test def exports_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Export")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowExports)
      .expectInvalidAtLines(7)
  }

  @Test def AllowXml_should_allow_xml(): Unit = {
    createTest(testController)
      .onFile("Xml")
      .withFeatures(
        AllowVals,
        AllowXml
      )
      .expectValid()
  }

  @Test def xml_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Xml")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowXml)
      .expectInvalidAtLines(2, 3)
  }

  @Test def AllowStringInterpolation_should_allow_string_interpolation(): Unit = {
    createTest(testController)
      .onFile("StringInterpolation")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowVals,
        AllowStringInterpolation
      )
      .expectValid()
  }

  @Test def string_interpolation_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("StringInterpolation")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowStringInterpolation)
      .expectInvalid(3 -> 1, 4 -> 1, 6 -> 3)
  }

  @Test def AllowAnnotations_should_allow_annotations(): Unit = {
    createTest(testController)
      .onFile("Annotations")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowDefs,
        AllowBasicOop,
        AllowAnnotations
      )
      .expectValid()
  }

  @Test def annotations_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Annotations")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowAnnotations)
      .expectInvalidAtLines(2, 5, 7, 8, 11)
  }

  @Test def AllowInfixes_should_allow_infixes(): Unit = {
    createTest(testController)
      .onFile("Infixes")
      .withFeatures(
        AllowDefs,
        AllowLiteralsAndExpressions,
        AllowInfixes
      )
      .expectValid()
  }

  @Test def infixes_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Infixes")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowInfixes)
      .expectInvalidAtLines(2)
  }

  @Test def AllowInlines_should_allow_inlines(): Unit = {
    createTest(testController)
      .onFile("Inlines")
      .withFeatures(
        AllowDefs,
        AllowLiteralsAndExpressions,
        AllowInlines
      )
      .expectValid()
  }

  @Test def inlines_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Inlines")
      .withFeatures(ALL_FEATURES)
      .exceptFeatures(AllowInlines)
      .expectInvalidAtLines(2)
  }

  @Test def checker_should_support_classes_without_braces(): Unit = {
    createTest(testController)
      .onFile("NoBraces")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowBasicOop,
        AllowDefs
      )
      .expectValid()
  }

}

object WhitelistCheckerTests {
  val testController = new TestController()

  @AfterClass
  def checkAllRun(): Unit = {
    testController.assertEmpty()
  }
}
