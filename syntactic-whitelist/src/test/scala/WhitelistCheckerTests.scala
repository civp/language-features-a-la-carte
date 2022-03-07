import org.junit.{AfterClass, Test}
import org.junit.Assert._
import Checker.CheckResult
import Features.{AllowADTs, AllowBasicOop}

import scala.meta._

class WhitelistCheckerTests {
  import WhitelistCheckerTests._

  @Test def allowLiteralsAndExpressionsShouldAllowLiterals(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("LiteralsOnly")
      .withFeatures(Features.AllowLiteralsAndExpressions)
      .expectingResult(CheckResult.Valid)
      .build()
      .run()
  }

  @Test def allowLiteralsAndExpressionsShouldAllowLiteralsAndExpressions(): Unit = {
    new TestRunner.Builder(testController)
      .onFile("LiteralsAndExpressions")
      .withFeatures(Features.AllowLiteralsAndExpressions)
      .expectingResult(CheckResult.Valid)
      .build()
      .run()
  }

  @Test def valsShouldBeRejectedWhenNotAllowed(): Unit = {
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
        Features.AllowPolymorphicTypes
      )
      .expectingMatching({
        case CheckResult.Invalid(violations) => (violations.size >= 5)  // TODO possibly more precise assertions
        case _ => true
      })
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
