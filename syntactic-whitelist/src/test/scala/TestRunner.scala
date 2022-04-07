import Checker.{CheckResult, Violation}
import org.junit.Assert.{assertEquals, assertTrue, fail}

import java.util.StringJoiner
import scala.meta.parsers.ParseException
import scala.meta.{Dialect, dialects}

object TestRunner {

  def expectValidWithFeatures(
                               srcFileName: String,
                               dialect: Dialect = defaultDialect,
                               features: List[Feature]
                             ): Unit = {
    runTest(srcFileName, expectValidCheckFunc, dialect, features)
  }

  def expectInvalidWhenExcludingFeatures(
                                          srcFileName: String,
                                          dialect: Dialect = defaultDialect,
                                          excludedFeatures: List[Feature],
                                          expectedViolationCnts: Map[Int, Int]
                                        ): Unit = {
    val allowedFeatures = Features.all.filter(!excludedFeatures.contains(_))
    runTest(srcFileName, expectInvalidCheckFunc(expectedViolationCnts), dialect, allowedFeatures)
  }

  def expectParsingError(
                          srcFileName: String,
                          dialect: Dialect = defaultDialect
                        ): Unit = {
    runTest(srcFileName, expectParsingErrorCheckFunc, dialect, Features.all)
  }

  private val defaultDialect = dialects.Scala3
  private val testResDir = "src/test/res"
  private val testFilesExtension = "scala"

  private def runTest(
                       srcFileName: String,
                       assertionFunc: Checker.CheckResult => Unit,
                       dialect: Dialect,
                       features: List[Feature]
                     ): Unit = {
    val checker = Checker(features)
    val filepath = s"$testResDir/$srcFileName.$testFilesExtension"
    val checkRes = checker.checkFile(dialect, filepath)
    assertionFunc(checkRes)
  }

  private val expectParsingErrorCheckFunc: PartialFunction[CheckResult, Unit] = {
    case err: CheckResult.ParsingError => {
      assertTrue(s"expected ParseException, got ${err.cause.getClass}", err.cause.isInstanceOf[ParseException])
    }
    case other => fail(s"expected CheckResult.ParsingError, got ${other.getClass}($other)")
  }

  private val expectValidCheckFunc: PartialFunction[CheckResult, Unit] = {
    case CheckResult.ParsingError(cause) => throw cause
    case res => assertEquals(res, CheckResult.Valid)
  }

  private def expectInvalidCheckFunc(expectedViolationsCnts: Map[Int, Int]): PartialFunction[CheckResult, Unit] = {

    // convert from 1-based to 0-based line indices
    val expectedViolationsCntZeroBasedLines = expectedViolationsCnts.map(lineAndCnt => (lineAndCnt._1 - 1, lineAndCnt._2))

    // extracts the number of actual violation(s) on each line
    def extractViolationsCnts(violations: List[Violation]): Map[Int, Int] = {
      violations.foldLeft(Map.empty[Int, Int])((acc, violation) => {
        val line = violation.tree.pos.startLine
        acc.updated(line, acc.getOrElse(line, default = 0) + 1)
      })
    }

    def assertViolationsCntsEqual(actualViolations: List[Violation]): Unit = {
      val actualViolationsCnts = extractViolationsCnts(actualViolations)
      val allLinesZeroBased = (expectedViolationsCntZeroBasedLines.keys ++ actualViolationsCnts.keys).toList.sorted
      val stringJoiner = new StringJoiner("\n")
      for (lineZeroBased <- allLinesZeroBased) {
        val expected = expectedViolationsCntZeroBasedLines.getOrElse(lineZeroBased, default = 0)
        val actual = actualViolationsCnts.getOrElse(lineZeroBased, default = 0)
        if (actual != expected) {
          stringJoiner.add(s"error at line ${lineZeroBased + 1}: unexpected number of violations found: $actual, expected: $expected\n" +
            s"\tfound violations are: ${actualViolations.filter(_.tree.pos.startLine == lineZeroBased)}")
        }
      }
      if (stringJoiner.length() > 0) {
        fail(stringJoiner.toString)
      }
    }

    require(expectedViolationsCnts.values.forall(_ >= 0))
    val partialFunction: PartialFunction[CheckResult, Unit] = {
      case CheckResult.ParsingError(cause) => throw cause
      case CheckResult.Valid => fail("checker accepted program but it should have rejected it")
      case CheckResult.Invalid(actualViolations) => assertViolationsCntsEqual(actualViolations)
    }
    partialFunction
  }

}

