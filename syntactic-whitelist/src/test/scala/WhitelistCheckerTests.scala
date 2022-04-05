import Features._
import TestRunner.createTest
import org.junit.{AfterClass, Test}

import scala.meta.dialects

class WhitelistCheckerTests {
  import WhitelistCheckerTests._

  @Test def allowLiteralsAndExpressions_should_allow_literals(): Unit = {
    createTest(testController)
      .onFile("LiteralsOnly")
      .withFeatures(LiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectValid()
  }

  @Test def allowLiteralsAndExpressions_should_allow_literals_and_expressions(): Unit = {
    createTest(testController)
      .onFile("LiteralsAndExpressions")
      .withFeatures(LiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectValid()
  }

  @Test def vals_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Vals")
      .withAllFeaturesExcept(Vals)
      .expectInvalid(2 -> 1, 3 -> 1, 4 -> 1, 5 -> 1, 6 -> 1)
  }

  @Test def vals_should_be_accepted_when_allowed(): Unit = {
    createTest(testController)
      .onFile("Vals")
      .withFeatures(
        LiteralsAndExpressions,
        Vals
      )
      .expectValid()
  }

  @Test def defs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Defs")
      .withAllFeaturesExcept(Defs)
      .expectInvalid(2 -> 1, 3 -> 1)
  }

  @Test def allowADTs_should_allow_ADTs(): Unit = {
    createTest(testController)
      .onFile("ADTs")
      .withFeatures(
        LiteralsAndExpressions,
        ADTs
      )
      .expectValid()
  }

  @Test def ADTs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("ADTs")
      .withAllFeaturesExcept(
        ADTs,
        BasicOop,
        AdvancedOop
      )
      .expectInvalid(2 -> 2, 4 -> 1, 5 -> 3, 6 -> 3,
        3 -> 1  // FIXME weird behavior of Scalameta on a trait without body
      )
  }

  @Test def AllowADTs_should_not_allow_sealed_trait_with_non_case_class(): Unit = {
    createTest(testController)
      .onFile("NotSoADT")
      .withFeatures(ADTs)
      .expectInvalid(2 -> 1, 3 -> 1)
  }

  @Test def allowBasicOop_should_allow_sealed_trait_with_non_case_class(): Unit = {
    createTest(testController)
      .onFile("NotSoADT")
      .withFeatures(BasicOop)
      .expectValid()
  }

  @Test def allowLiteralFunctions_should_allow_literal_functions(): Unit = {
    createTest(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(
        LiteralFunctions,
        LiteralsAndExpressions,
        Vals
      )
      .expectValid()
  }

  @Test def literal_functions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withAllFeaturesExcept(LiteralFunctions)
      .expectInvalid(2 -> 1, 3 -> 1, 4 -> 2, 5 -> 1, 6 -> 3, 7 -> 1)
  }

  @Test def allowForExpr_should_allow_for_expressions(): Unit = {
    createTest(testController)
      .onFile("Fors")
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        ForExpr
      )
      .expectValid()
  }

  @Test def for_expressions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Fors")
      .withAllFeaturesExcept(ForExpr)
      .expectInvalid(3 -> 2, 4 -> 2, 5 -> 2)
  }

  @Test def AllowImports_should_allow_imports(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withFeatures(Imports)
      .expectValid()
  }

  @Test def imports_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withAllFeaturesExcept(Imports)
      .expectInvalid(1 -> 1)
  }

  @Test def AllowPackages_should_allow_packages(): Unit = {
    createTest(testController)
      .onFile("PackageOnly")
      .withFeatures(Packages)
      .expectValid()
  }

  @Test def AllowNull_should_allow_null(): Unit = {
    createTest(testController)
      .onFile("Nulls")
      .withFeatures(
        LiteralsAndExpressions,
        Vals,
        Nulls
      )
      .expectValid()
  }

  @Test def nulls_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Nulls")
      .withAllFeaturesExcept(Nulls)
      .expectInvalid(1 -> 1, 2 -> 1, 4 -> 1)
  }

  @Test def AllowPolymorphicTypes_should_allow_polymorphic_types(): Unit = {
    createTest(testController)
      .onFile("Polymorphism")
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        BasicOop,
        PolymorphicTypes
      )
      .expectValid()
  }

  @Test def polymorphic_types_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Polymorphism")
      .withAllFeaturesExcept(PolymorphicTypes)
      .expectInvalid(2 -> 2, 4 -> 2, 7 -> 1, 11 -> 2)
  }

  @Test def AllowLaziness_should_allow_lazy_vals_and_args(): Unit = {
    createTest(testController)
      .onFile("Laziness")
      .withFeatures(
        Vals,
        Defs,
        LiteralsAndExpressions,
        Laziness
      )
      .expectValid()
  }

  @Test def laziness_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Laziness")
      .withAllFeaturesExcept(Laziness)
      .expectInvalidAtLines(2, 3)
  }

  @Test def AllowAdvancedOop_should_allow_advanced_oop_constructs(): Unit = {
    createTest(testController)
      .onFile("AdvancedOOP")
      .withFeatures(
        LiteralsAndExpressions,
        AdvancedOop,
        Defs
      )
      .expectValid()
  }

  @Test def advanced_oop_constructs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("AdvancedOOP")
      .withAllFeaturesExcept(AdvancedOop)
      .expectInvalidAtLines(2, 6, 10)
  }
  
  @Test def AllowImperativeConstructs_should_allow_imperative_constructs(): Unit = {
    createTest(testController)
      .onFile("Imperative")
      .withDialect(dialects.Sbt1)
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        ImperativeConstructs
      )
      .expectValid()
  }

  @Test def imperative_constructs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Imperative")
      .withDialect(dialects.Sbt1)
      .withAllFeaturesExcept(ImperativeConstructs)
      .expectInvalidAtLines(2, 3, 5, 7, 8, 14, 15, 18)
  }

  @Test def AllowContextualConstructs_should_allow_contextual_constructs(): Unit = {
    createTest(testController)
      .onFile("Contextual")
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        ContextualConstructs,
        PolymorphicTypes
      )
      .expectValid()
  }

  @Test def contextual_constructs_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Contextual")
      .withAllFeaturesExcept(ContextualConstructs)
      .expectInvalidAtLines(3, 6, 8)
  }

  @Test def AllowExtensions_should_allow_extensions(): Unit = {
    createTest(testController)
      .onFile("Extensions")
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        Extensions
      )
      .expectValid()
  }

  @Test def extensions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Extensions")
      .withAllFeaturesExcept(Extensions)
      .expectInvalidAtLines(2)
  }

  @Test def AllowMetaprogramming_should_allow_macros(): Unit = {
    createTest(testController)
      .onFile("Metaprogramming")
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        ContextualConstructs,
        PolymorphicTypes,
        Metaprogramming
      )
      .expectValid()
  }

  @Test def metaprogramming_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Metaprogramming")
      .withAllFeaturesExcept(Metaprogramming)
      .expectInvalid(5 -> 3)
  }

  @Test def AllowExports_should_allow_exports(): Unit = {
    createTest(testController)
      .onFile("Export")
      .withFeatures(
        LiteralsAndExpressions,
        BasicOop,
        Defs,
        Exports
      )
      .expectValid()
  }

  @Test def exports_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Export")
      .withAllFeaturesExcept(Exports)
      .expectInvalidAtLines(7)
  }

  @Test def AllowXml_should_allow_xml(): Unit = {
    createTest(testController)
      .onFile("Xml")
      .withFeatures(
        Vals,
        Xml
      )
      .expectValid()
  }

  @Test def xml_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Xml")
      .withAllFeaturesExcept(Xml)
      .expectInvalidAtLines(2, 3)
  }

  @Test def AllowStringInterpolation_should_allow_string_interpolation(): Unit = {
    createTest(testController)
      .onFile("StringInterpolation")
      .withFeatures(
        LiteralsAndExpressions,
        Vals,
        StringInterpolation
      )
      .expectValid()
  }

  @Test def string_interpolation_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("StringInterpolation")
      .withAllFeaturesExcept(StringInterpolation)
      .expectInvalid(3 -> 1, 4 -> 1, 6 -> 3)
  }

  @Test def AllowAnnotations_should_allow_annotations(): Unit = {
    createTest(testController)
      .onFile("Annotations")
      .withFeatures(
        LiteralsAndExpressions,
        Defs,
        BasicOop,
        Annotations
      )
      .expectValid()
  }

  @Test def annotations_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Annotations")
      .withAllFeaturesExcept(Annotations)
      .expectInvalidAtLines(2, 5, 7, 8, 11)
  }

  @Test def AllowInfixes_should_allow_infixes(): Unit = {
    createTest(testController)
      .onFile("Infixes")
      .withFeatures(
        Defs,
        LiteralsAndExpressions,
        Infixes
      )
      .expectValid()
  }

  @Test def infixes_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Infixes")
      .withAllFeaturesExcept(Infixes)
      .expectInvalidAtLines(2)
  }

  @Test def AllowInlines_should_allow_inlines(): Unit = {
    createTest(testController)
      .onFile("Inlines")
      .withFeatures(
        Defs,
        LiteralsAndExpressions,
        Inlines
      )
      .expectValid()
  }

  @Test def inlines_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Inlines")
      .withAllFeaturesExcept(Inlines)
      .expectInvalidAtLines(2)
  }

  @Test def checker_should_support_classes_without_braces(): Unit = {
    createTest(testController)
      .onFile("NoBraces")
      .withFeatures(
        LiteralsAndExpressions,
        BasicOop,
        Defs
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
