import Features.{AllowADTs, AllowBasicOop, AllowLiteralsAndExpressions, AllowVals}
import org.junit.{AfterClass, Test}
import scala.meta.dialects

class WhitelistCheckerTests {
  import WhitelistCheckerTests._
  import TestRunner.createTest

  @Test def allowLiteralsAndExpressions_should_allow_literals(): Unit = {
    createTest(testController)
      .onFile("LiteralsOnly")
      .withFeatures(Features.AllowLiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectValid()
  }

  @Test def allowLiteralsAndExpressions_should_allow_literals_and_expressions(): Unit = {
    createTest(testController)
      .onFile("LiteralsAndExpressions")
      .withFeatures(Features.AllowLiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectValid()
  }

  @Test def vals_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Vals")
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowVals)
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
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowDefs)
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
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(
        Features.AllowADTs,
        Features.AllowBasicOop,
        Features.AllowAdvancedOop
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
        Features.AllowLiteralFunctions,
        Features.AllowLiteralsAndExpressions,
        Features.AllowVals
      )
      .expectValid()
  }

  @Test def literal_functions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowLiteralFunctions)
      .expectInvalid(2 -> 1, 3 -> 1, 4 -> 2, 5 -> 1, 6 -> 3, 7 -> 1)
  }

  @Test def allowForExpr_should_allow_for_expressions(): Unit = {
    createTest(testController)
      .onFile("Fors")
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowDefs,
        Features.AllowForExpr
      )
      .expectValid()
  }

  @Test def for_expressions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Fors")
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowForExpr)
      .expectInvalid(3 -> 2, 4 -> 2, 5 -> 2)
  }

  @Test def AllowImports_should_allow_imports(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withFeatures(Features.AllowImports)
      .expectValid()
  }

  @Test def imports_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowImports)
      .expectInvalid(1 -> 3)
  }

  @Test def AllowPackages_should_allow_packages(): Unit = {
    createTest(testController)
      .onFile("PackageOnly")
      .withFeatures(Features.AllowPackages)
      .expectValid()
  }

  @Test def AllowNull_should_allow_null(): Unit = {
    createTest(testController)
      .onFile("Nulls")
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowVals,
        Features.AllowNull
      )
      .expectValid()
  }

  @Test def nulls_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Nulls")
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowNull)
      .expectInvalid(1 -> 1, 2 -> 1, 4 -> 1)
  }

  @Test def AllowPolymorphicTypes_should_allow_polymorphic_types(): Unit = {
    createTest(testController)
      .onFile("Polymorphism")
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowDefs,
        Features.AllowBasicOop,
        Features.AllowPolymorphicTypes
      )
      .expectValid()
  }

  @Test def polymorphic_types_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Polymorphism")
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowPolymorphicTypes)
      .expectInvalid(2 -> 2, 4 -> 2, 7 -> 1, 11 -> 2)
  }

  @Test def AllowLaziness_should_allow_lazy_vals_and_args(): Unit = {
    createTest(testController)
      .onFile("Laziness")
      .withFeatures(
        Features.AllowVals,
        Features.AllowDefs,
        Features.AllowLiteralsAndExpressions,
        Features.AllowLaziness
      )
      .expectValid()
  }

  @Test def laziness_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("Laziness")
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowLaziness)
      .expectInvalid(2 -> 1, 3 -> 1)
  }

}

object WhitelistCheckerTests {
  val testController = new TestController()

  @AfterClass
  def checkAllRun(): Unit = {
    testController.assertEmpty()
  }
}
