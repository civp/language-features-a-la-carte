import Checker.CheckResult
import Checker.CheckResult.ParsingError
import org.junit.Assert.{assertEquals, assertTrue, fail}

import java.util.StringJoiner
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.meta.{Dialect, dialects}

/**
 * Test runner for whitelist checker tests
 * @param matcher the function (consumer) that will be called by run; should contain the assertions
 * @param filename the name of the file in /test/res containing the program that should be given to the checker in the test
 *                 (actual name will be src/test/res/~filename~.scala
 * @param features the features to be used by the checker
 * @param dialect the dialect to be used by the checker
 * @param testController the TestController that ensures that no test passes only because checks are not executed
 * @param uid the unique identifier of the test
 */
class TestRunner private(matcher: CheckResult => Unit, filename: String, features: List[Feature], dialect: Dialect,
                         testController: TestController, uid: Int) {
  import TestRunner._

  // Actually execute the test and unregisters the test from the testController
  // To be called once everything is set up
  private def run(): Unit = {
    testController.unregisterTest(uid)
    val checker = Checker(features)
    val actualRes = checker.checkFile(dialect, s"$TEST_RES_DIR/$filename.$TEST_FILES_EXT")
    matcher(actualRes)
  }

}

object TestRunner {

  private val uidGenerator = new AtomicInteger(0)

  private val DEFAULT_DIALECT = dialects.Scala3
  private val TEST_RES_DIR = "src/test/res"
  private val TEST_FILES_EXT = "scala"

  // index of the frame of the StackWalker that corresponds to the test name, assuming that StackWalker was
  // instantiated in createTest
  private val TEST_METHOD_FRAME_IDX = 1

  /**
   * @return a builder for TestRunner
   * @param testController the controller that will be used to ensure that the test is actually run <p>
   * By default it will use Scala3 as a dialect, but this can be overriden <p>
   * <strong> This method should be called at top level inside the tests, it should not be nested
   * (for error reporting to work correctly) </strong>
   */
  def createTest(testController: TestController): Builder = {
    val testName = StackWalker.getInstance().walk(frames => {
      val frame = frames.limit(TEST_METHOD_FRAME_IDX+1).toList.get(TEST_METHOD_FRAME_IDX)
      frame.getMethodName
    })
    new Builder(testController, testName)
  }

  // builder for TestRunner
  class Builder protected[TestRunner](testController: TestController, testName: String) {
    private var matcher: Option[CheckResult => Unit] = None
    private var filename: Option[String] = None
    private val features: ListBuffer[Feature] = ListBuffer.empty
    private var dialect: Option[Dialect] = None
    private val uid = uidGenerator.incrementAndGet()

    {
      // tell the controller that a test with this uid must be executed
      testController.registerTest(uid, testName)
    }

    /**
     * @param _filename name of the file to run the checker on (name only, without directory or extension)
     */
    def onFile(_filename: String): Builder = {
      if (this.filename.isDefined){
        throw new IllegalStateException("filename set more than once")
      }
      this.filename = Some(_filename)
      this
    }

    /**
     * @param _dialect the Scala dialect to use
     */
    def withDialect(_dialect: Dialect): Builder = {
      if (this.dialect.isDefined){
        throw new IllegalStateException("dialect is set more than once")
      }
      this.dialect = Some(_dialect)
      this
    }

    /**
     * @param _features features that the Checker should allow
     */
    def withFeatures(_features: Feature*): Builder = {
      this.features.addAll(_features)
      this
    }

    /**
     * @param _features features that the Checker should allow
     */
    def withFeatures(_features: List[Feature]): Builder = {
      this.features.addAll(_features)
      this
    }

    /**
     * @param _features features that the Checker should allow
     */
    def withAllFeaturesExcept(_features: Feature*): Builder = {
      this.features.addAll(Features.all.filter(!_features.contains(_)))
      this
    }

    /**
     * @param matcher function that will be called on the resulting CheckResult, in order to check its correctness
     */
    private def expectMatching(matcher: CheckResult => Unit): Unit = {
      if (this.matcher.isDefined){
        throw new IllegalStateException("matcher set more than once")
      }
      this.matcher = Some(matcher)
      build().run()
    }

    def expectParsingError[T <: Throwable](expected: Class[T]): Unit = {
      expectMatching {
        case err: CheckResult.ParsingError => {
          val actualClass = err.cause.getClass
          assertEquals(s"expected $expected, got $actualClass", expected, actualClass)
        }
        case other => fail(s"expected CheckResult.ParsingError, got ${other.getClass}($other)")
      }
    }

    /**
     * @param expected expected test output, will be compared to the actual one using assertEquals
     */
    private def expectResult(expected: CheckResult): Unit = {
      if (this.matcher.isDefined){
        throw new IllegalStateException("matcher set more than once")
      }
      this.matcher = Some {
        case ParsingError(cause) => throw cause
        case actual => assertEquals(expected, actual)
      }
      build().run()
    }

    /**
     * Fails the test if the result is not valid
     */
    def expectValid(): Unit = {
      expectResult(CheckResult.Valid)
    }

    /**
     * If the result of the check is invalid, calls the provided assertion on it,
     * o.w. fails the test
     * @param assertion the assertion to be runned on the result if it is invalid
     */
    private def expectInvalidWithAssertion(assertion: CheckResult.Invalid => Unit): Unit = {
      expectMatching {
        case CheckResult.Valid => fail("checker validated program but it should reject it")
        case invalid: CheckResult.Invalid => assertion(invalid)
        case CheckResult.ParsingError(e) => throw e
      }
    }

    /**
     * Fails the test if either the result of the check is not an instance of Invalid or if the reported violations
     * do not conform to the map given as an argument
     * @param expectedViolationsCnt map that associates each line to the expected number of violations on that line
     *                              (lines with 0 expected violations can be omitted)<p>
     *                              e.g. 5 -> 2, 7 -> 1 means 2 violations at line 5 and 1 at line 7
     */
    def expectInvalid(expectedViolationsCnt: Map[Int, Int]): Unit = {
      require(expectedViolationsCnt.values.forall(_ >= 0))

      // convert from 1-based to 0-based line indices
      val expectedViolationsCntZeroBasedLines = expectedViolationsCnt.map(lineAndCnt => (lineAndCnt._1 - 1, lineAndCnt._2))

      expectInvalidWithAssertion { invalid =>

        // extract the number of actual violation(s) on each line
        val violationsWithLines = invalid.violations
          .foldLeft(Map.empty[Int, Int])((acc, violation) => {
            val line = violation.tree.pos.startLine
            acc.updated(line, acc.getOrElse(line, default = 0) + 1)
          })

        val allLinesZeroBased = (expectedViolationsCntZeroBasedLines.keys ++ violationsWithLines.keys).toList.sorted
        val stringJoiner = new StringJoiner("\n")
        for (lineZeroBased <- allLinesZeroBased) {
          val expected = expectedViolationsCntZeroBasedLines.getOrElse(lineZeroBased, default = 0)
          val actual = violationsWithLines.getOrElse(lineZeroBased, default = 0)
          if (actual != expected){
            stringJoiner.add(s"error at line ${lineZeroBased+1}: unexpected number of violations found: $actual, expected: $expected\n" +
              s"\tfound violations are: ${invalid.violations.filter(_.tree.pos.startLine == lineZeroBased)}")
          }
        }
        if (stringJoiner.length() > 0){
          fail(stringJoiner.toString)
        }
      }
    }

    /**
     * Fails the test if either the result of the check is not an instance of Invalid or if the reported violations
     * do not conform to the pairs given as an argument
     * @param expectedViolations pairs that associate each line to the expected number of violations on that line
     *                              (lines with 0 expected violations can be omitted) <p>
     *                              e.g. 5 -> 2, 7 -> 1 means 2 violations at line 5 and 1 at line 7
     */
    def expectInvalid(expectedViolations: (Int, Int)*): Unit = expectInvalid(expectedViolations.toMap)

    /**
     * Fails the test if either the result of the check is not an instance of Invalid or if the reported violations
     * do not conform to the list of lines given as an argument
     * @param expectedViolationLines each occurrence of a line index in this list is considered as an expected
     *                               violation on that line <p>
     *                               e.g. [5, 5, 7] means 2 violations at line 5 and 1 at line 7
     */
    def expectInvalidAtLines(expectedViolationLines: Int*): Unit = {
      val expectedViolationsAsMap =
        (for (line <- expectedViolationLines) yield line -> expectedViolationLines.count(_ == line)).toMap
      expectInvalid(expectedViolationsAsMap)
    }

    /**
     * @return an instance of TestRunner with the provided test execution steps
     */
    private def build(): TestRunner = {
      if (filename.isEmpty) throw new IllegalStateException("cannot build TestRunner: no filename")
      if (matcher.isEmpty) throw new IllegalStateException("cannot build TestRunner: no expectedRes")
      if (features.isEmpty) throw new IllegalStateException("cannot build TestRunner: no features")
      new TestRunner(
        matcher.get,
        filename.get,
        features.toList,
        dialect.getOrElse(DEFAULT_DIALECT),
        testController,
        uid
      )
    }

  }

}
