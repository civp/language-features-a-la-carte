package syntactic_checker.whitelist

import org.junit.Assert.{assertEquals, assertTrue, fail}
import syntactic_checker.whitelist.Features._
import syntactic_checker.whitelist.WhitelistChecker.WhitelistViolation
import syntactic_checker.{CheckResult, Violation}

import java.util.StringJoiner
import scala.meta.io.AbsolutePath
import scala.meta.parsers.ParseException
import scala.meta.{Dialect, dialects}

object WhitelistCheckerTestRunner {

  /**
   * Test method for a test where the checker should accept the program
   *
   * @param srcFileName name of the file containing the program <p>
   *                    This is the name of one of the files in test/res, without ".scala"
   * @param dialect     the dialect to be used by the checker
   * @param features    the features that the checker should use
   */
  def expectValidWithFeatures(
                               srcFileName: String,
                               dialect: Dialect = defaultDialect,
                               features: List[Feature]
                             ): Unit = {
    runTest(srcFileName, expectValidCheckFunc, dialect, features)
  }

  /**
   * Test method for a test where the checker should reject the program
   *
   * @param srcFileName           name of the file containing the program <p>
   *                              This is the name of one of the files in test/res, without ".scala"
   * @param dialect               the dialect to be used by the checker
   * @param excludedFeatures      the checker will use all atomic features except these ones
   * @param expectedViolationCnts map from line indices to the number of expected violations on the
   *                              corresponding line. Lines with 0 expected violation can be omitted
   */
  def expectInvalidWhenExcludingFeatures(
                                          srcFileName: String,
                                          dialect: Dialect = defaultDialect,
                                          excludedFeatures: List[Feature],
                                          expectedViolationCnts: Map[Int, Int]
                                        ): Unit = {
    val allowedFeatures = allFeatures.filter(!excludedFeatures.contains(_))
    runTest(srcFileName, expectInvalidCheckFunc(expectedViolationCnts), dialect, allowedFeatures)
  }

  /**
   * Test method for a test where the parsing of the program should fail <p>
   * The method therefore expects a syntactic_checker.CheckResult.ParsingError to be returned by the check method
   *
   * @param srcFileName name of the file containing the program <p>
   *                    This is the name of one of the files in test/res, without ".scala"
   * @param dialect     the dialect to be used by the checker
   */
  def expectParsingError(
                          srcFileName: String,
                          dialect: Dialect = defaultDialect
                        ): Unit = {
    runTest(srcFileName, expectParsingErrorCheckFunc, dialect, allFeatures)
  }

  private val defaultDialect = dialects.Scala3
  private val testResourcesDirectory = "src/test/res"
  private val testFilesExtension = "scala"

  /**
   * Runs the test described by its arguments
   *
   * @param srcFileName   name of the file containing the program <p>
   *                      This is the name of one of the files in test/res, without ".scala"
   * @param assertionFunc the function that is called on the syntactic_checker.CheckResult returned by the check method <p>
   *                      This function should contain the assertions
   * @param dialect       the dialect to be used by the checker
   * @param features      the features that the checker should use
   */
  private def runTest(
                       srcFileName: String,
                       assertionFunc: CheckResult[WhitelistViolation] => Unit,
                       dialect: Dialect,
                       features: List[Feature]
                     ): Unit = {
    val checker = WhitelistChecker(features)
    // FIXME should probably not be necessary to have both paths
    val intellijTestFile = AbsolutePath(s"$testResourcesDirectory/$srcFileName.$testFilesExtension").toFile

    def sbtTestFile = AbsolutePath(s"syntactic/$testResourcesDirectory/$srcFileName.$testFilesExtension").toFile

    val file = if (intellijTestFile.exists()) intellijTestFile else sbtTestFile
    val checkRes = checker.checkFile(dialect, file)
    assertionFunc(checkRes)
  }

  /**
   * Assertion function for parsing error
   */
  private val expectParsingErrorCheckFunc: PartialFunction[CheckResult[WhitelistViolation], Unit] = {
    case err: CheckResult.ParsingError =>
      assertTrue(s"expected ParseException, got ${err.cause.getClass}", err.cause.isInstanceOf[ParseException])
    case other => fail(s"expected syntactic_checker.CheckResult.ParsingError, got ${other.getClass}($other)")
  }

  /**
   * Assertion function for valid program
   */
  private val expectValidCheckFunc: PartialFunction[CheckResult[WhitelistViolation], Unit] = {
    case CheckResult.ParsingError(cause) => throw cause
    case res => assertEquals(res, CheckResult.Valid)
  }

  /**
   * Produces an assertion function for invalid programs
   *
   * @param expectedViolationsCnts map from line indices to the number of expected violations on the
   *                               corresponding line. Lines with 0 expected violation can be omitted
   */
  private def expectInvalidCheckFunc(expectedViolationsCnts: Map[Int, Int]): PartialFunction[CheckResult[WhitelistViolation], Unit] = {

    // convert from 1-based to 0-based line indices
    val expectedViolationsCntZeroBasedLines = expectedViolationsCnts.map(lineAndCnt => (lineAndCnt._1 - 1, lineAndCnt._2))

    // extracts the number of actual violation(s) on each line
    def extractViolationsCnts(violations: List[WhitelistViolation]): Map[Int, Int] = {
      violations.foldLeft(Map.empty[Int, Int])((acc, violation) => {
        val line = violation.forbiddenNode.pos.startLine
        acc.updated(line, acc.getOrElse(line, default = 0) + 1)
      })
    }

    def assertViolationsCntsEqual(actualViolations: List[WhitelistViolation]): Unit = {
      val actualViolationsCnts = extractViolationsCnts(actualViolations)
      val allLinesZeroBased = (expectedViolationsCntZeroBasedLines.keys ++ actualViolationsCnts.keys).toList.sorted
      val stringJoiner = new StringJoiner("\n")
      for (lineZeroBased <- allLinesZeroBased) {
        val expected = expectedViolationsCntZeroBasedLines.getOrElse(lineZeroBased, default = 0)
        val actual = actualViolationsCnts.getOrElse(lineZeroBased, default = 0)
        if (actual != expected) {
          stringJoiner.add(s"error at line ${lineZeroBased + 1}: unexpected number of violations found: $actual, expected: $expected\n" +
            s"\tfound violations are: ${actualViolations.filter(_.forbiddenNode.pos.startLine == lineZeroBased)}")
        }
      }
      if (stringJoiner.length() > 0) {
        fail(stringJoiner.toString)
      }
    }

    require(expectedViolationsCnts.values.forall(_ >= 0))
    val partialFunction: PartialFunction[CheckResult[WhitelistViolation], Unit] = {
      case CheckResult.ParsingError(cause) => throw cause
      case CheckResult.Valid => fail("checker accepted program but it should have rejected it")
      case CheckResult.Invalid(actualViolations) if actualViolations.forall(_.isInstanceOf[WhitelistViolation]) =>
        assertViolationsCntsEqual(actualViolations)
    }
    partialFunction
  }

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

}