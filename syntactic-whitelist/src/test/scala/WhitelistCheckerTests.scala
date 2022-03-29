import Features.{AllowADTs, AllowBasicOop, AllowLiteralsAndExpressions, AllowVals}
import org.junit.Assert._
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
      .expectInvalidWithAssertion { invalid =>
        assertTrue(invalid.violations.size >= 5)  // TODO possibly more precise assertions
      }
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
      .expectInvalidWithAssertion { invalid =>
        assertTrue(invalid.violations.size >= 2)  // TODO possibly more precise assertions
      }
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
      .expectInvalidWithAssertion { invalid =>
        assertTrue(invalid.violations.size >= 3)
      }
  }

  @Test def AllowADTs_should_not_allow_sealed_trait_with_non_case_class(): Unit = {
    createTest(testController)
      .onFile("NotSoADT")
      .withFeatures(AllowADTs)
      .expectInvalidWithAssertion { invalid =>
        invalid.violations.nonEmpty
      }
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
        Features.AllowVals,
        Features.AllowAnonymousFunctions
      )
      .expectValid()
  }

  @Test def literal_functions_should_be_rejected_when_not_allowed(): Unit = {
    createTest(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(Features.ALL_FEATURES)
      .exceptFeatures(Features.AllowLiteralFunctions)
      .expectInvalidWithAssertion { invalid =>
        invalid.violations.size >= 6  // TODO possibly more precise assertion
      }
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
      .expectInvalidWithAssertion { invalid =>
        invalid.violations.size >= 3  // TODO possibly more precise assertion
      }
  }

  @Test def AllowImports_should_allow_imports(): Unit = {
    createTest(testController)
      .onFile("ImportOnly")
      .withFeatures(Features.AllowImports)
      .expectValid()
  }

  @Test def AllowPackages_should_allow_packages(): Unit = {
    createTest(testController)
      .onFile("PackageOnly")
      .withFeatures(Features.AllowPackages)
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
