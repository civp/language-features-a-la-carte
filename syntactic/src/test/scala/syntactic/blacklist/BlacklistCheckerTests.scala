package syntactic.blacklist

import org.junit.Assert._
import org.junit.Test
import syntactic.CheckResult

import scala.meta.dialects

class BlacklistCheckerTests {

  // TODO test requiring to load a file
  // TODO cleaner tests

  @Test
  def nullInstanceOfCheckerTest(): Unit = {
    val checker = BlacklistChecker(PredefBlacklistRules.NoNull, PredefBlacklistRules.NoCast)
    val checkRes = checker.checkCodeString(dialects.Scala213, sourceCode)
    checkRes match {
      case CheckResult.ParsingError(e) => throw e
      case CheckResult.Valid => fail("should not be valid")
      case CheckResult.Invalid(detectedViolations) =>
        val expected = List(
          (11, 48),
          (12, 51),
          (22, 18),
          (23, 20)
        )
        assertEquals(expected, detectedViolations.map(violation => (violation.startLine, violation.startColumn)))
    }
  }

  @Test
  def varWhileCheckerTest(): Unit = {
    val checker = BlacklistChecker(PredefBlacklistRules.NoVar, PredefBlacklistRules.NoWhile)
    val checkRes = checker.checkCodeString(dialects.Scala213, sourceCode)
    checkRes match {
      case CheckResult.ParsingError(e) => throw e
      case CheckResult.Valid => fail("should not be valid")
      case CheckResult.Invalid(detectedViolations) =>
        val expected = List(
          (2, 2),
          (3, 2),
          (16, 6),
          (17, 6),
          (20, 8),
          (26, 6)
        )
        assertEquals(expected, detectedViolations.map(violation => (violation.startLine, violation.startColumn)))
    }
  }

  val sourceCode: String =
    """
      |abstract class Example {
      |  private var w: Int = 0
      |  var y: Int
      |  val z: String
      |
      |  def f(x: Int, y: Int): Int = 2*x+y+x*x*x
      |
      |  def foo(str: String, bar: Option[String]): String = {
      |    if (str.length > 5){
      |      bar match {
      |        case Some(value) => value ++ str ++ s"${null}"
      |        case None => if (str.length < 10) str else null
      |      }
      |    }
      |    else {
      |      var x = 0
      |      while (x < 1000){
      |        x = x + (2*x-5) % 15
      |        y = 2
      |        while (y > -50){
      |          y = y - 1
      |          val p = null
      |          y = y + p.asInstanceOf[Int]
      |        }
      |      }
      |      do {
      |        y = y + 5
      |      } while (y < 0)
      |      y.toString
      |    }
      |  }
      |
      |}
      |
      |""".stripMargin

}
