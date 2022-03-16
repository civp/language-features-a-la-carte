import Features.{AllowADTs, AllowBasicOop, AllowLiteralsAndExpressions, AllowVals}
import org.junit.Assert._
import org.junit.{AfterClass, Test}
import scala.meta.dialects

class WhitelistCheckerTests {
  import WhitelistCheckerTests._

  @Test def allowLiteralsAndExpressions_should_allow_literals(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("LiteralsOnly")
      .withFeatures(Features.AllowLiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectingValid()
      .build()
      .run()
  }

  @Test def allowLiteralsAndExpressions_should_allow_literals_and_expressions(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("LiteralsAndExpressions")
      .withFeatures(Features.AllowLiteralsAndExpressions)
      .withDialect(dialects.Sbt1)
      .expectingValid()
      .build()
      .run()
  }

  @Test def vals_should_be_rejected_when_not_allowed(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("Vals")
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowDefs,
        Features.AllowForExpr,
        Features.AllowAdvancedOop,
        Features.AllowImperativeConstructs,
        Features.AllowLaziness,
        Features.AllowLiteralFunctions,
        Features.AllowPolymorphicTypes,
      )
      .expectingInvalidWithAssertion { invalid =>
        assertTrue(invalid.violations.size >= 5)  // TODO possibly more precise assertions
      }
      .build()
      .run()
  }

  @Test def vals_should_be_accepted_when_allowed(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("Vals")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowVals
      )
      .expectingValid()
      .build()
      .run()
  }

  @Test def defs_should_be_rejected_when_not_allowed(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("Defs")
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowVals,
        Features.AllowForExpr,
        Features.AllowAdvancedOop,
        Features.AllowImperativeConstructs,
        Features.AllowLaziness,
        Features.AllowLiteralFunctions,
        Features.AllowPolymorphicTypes
      )
      .expectingInvalidWithAssertion { invalid =>
        assertTrue(invalid.violations.size >= 2)  // TODO possibly more precise assertions
      }
      .build()
      .run()
  }

  @Test def allowADTs_should_allow_ADTs(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("ADTs")
      .withFeatures(
        AllowLiteralsAndExpressions,
        AllowADTs
      )
      .expectingValid()
      .build()
      .run()
  }

  @Test def ADTs_should_be_rejected_when_not_allowed(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("ADTs")
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowVals,
        Features.AllowForExpr,
        Features.AllowImperativeConstructs,
        Features.AllowLaziness,
        Features.AllowLiteralFunctions,
        Features.AllowPolymorphicTypes
      )
      .expectingInvalidWithAssertion { invalid =>
        assertTrue(invalid.violations.size >= 3)
      }
      .build()
      .run()
  }

  @Test def AllowADTs_should_not_allow_sealed_trait_with_non_case_class(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("NotSoADT")
      .withFeatures(AllowADTs)
      .expectingInvalidWithAssertion { invalid =>
        invalid.violations.nonEmpty
      }
      .build()
      .run()
  }

  @Test def allowBasicOop_should_allow_sealed_trait_with_non_case_class(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("NotSoADT")
      .withFeatures(AllowBasicOop)
      .expectingValid()
      .build()
      .run()
  }

  @Test def allowLiteralFunctions_should_allow_literal_functions(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(
        Features.AllowLiteralFunctions,
        Features.AllowLiteralsAndExpressions,
        Features.AllowVals,
        Features.AllowAnonymousFunctions
      )
      .expectingValid()
      .build()
      .run()
  }

  @Test def literal_functions_should_be_rejected_when_not_allowed(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("LiteralFunctions")
      .withDialect(dialects.Sbt1)
      .withFeatures(
        Features.AllowLiteralsAndExpressions,
        Features.AllowVals,
        Features.AllowDefs,
        Features.AllowAdvancedOop,
        Features.AllowPolymorphicTypes,
        Features.AllowForExpr,
        Features.AllowAnonymousFunctions,
        Features.AllowImperativeConstructs,
        Features.AllowContextualConstructs,
        Features.AllowExports,
        Features.AllowImports,
        Features.AllowExtensions,
        Features.AllowMacros,
        Features.AllowNull
      )
      .expectingInvalidWithAssertion { invalid =>
        invalid.violations.size >= 6  // TODO possibly more precise assertion
      }
      .build()
      .run()
  }

}

object WhitelistCheckerTests {
  val testController = new TestController()

  @AfterClass
  def checkAllRun(): Unit = {
    testController.assertEmpty()
  }
}
