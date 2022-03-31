import Checker.CheckResult
import Checker.CheckResult.ParsingError
import org.junit.Assert.{assertEquals, assertTrue, fail}

import java.util.StringJoiner
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.ListBuffer
import scala.meta.{Dialect, dialects}

/**
 * Test runner for whitelist checker tests
 */
class TestRunner private(matcher: CheckResult => Unit, filename: String, features: List[Feature], dialect: Dialect,
                         testController: TestController, uid: Int) {
  import TestRunner._

  /**
   * Execute the tests
   */
  def run(): Unit = {
    testController.unregisterTest(uid)
    val checker = Checker(dialect, features)
    val actualRes = checker.checkFile(s"$TEST_RES_DIR/$filename.$TEST_FILES_EXT")
    matcher(actualRes)
  }

}

object TestRunner {

  private val uidGenerator = new AtomicInteger(0)

  private val DEFAULT_DIALECT = dialects.Scala3
  private val TEST_RES_DIR = "src/test/res"
  private val TEST_FILES_EXT = "scala"

  private val TEST_METHOD_FRAME_IDX = 1

  /**
   * @return a builder for TestRunner
   * @param testController the controller that will be used to ensure that the test is actually run
   * By default it will use Scala3 as a dialect, but it can be overriden <p>
   * This method should be called at top level inside the tests, not nested (for error reporting to work correctly)
   */
  def createTest(testController: TestController): Builder = {
    val testName = StackWalker.getInstance().walk(frames => {
      val frame = frames.limit(TEST_METHOD_FRAME_IDX+1).toList.get(TEST_METHOD_FRAME_IDX)
      frame.getMethodName
    })
    new Builder(testController, testName)
  }

  class Builder protected[TestRunner](testController: TestController, testName: String) {
    private var matcher: Option[CheckResult => Unit] = None
    private var filename: Option[String] = None
    private val features: ListBuffer[Feature] = ListBuffer.empty
    private var dialect: Option[Dialect] = None
    private val uid = uidGenerator.incrementAndGet()

    {
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
    def exceptFeatures(_features: Feature*): Builder = {
      this.features.filterInPlace(!_features.contains(_))
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

    def expectValid(): Unit = {
      expectResult(CheckResult.Valid)
    }

    private def expectInvalidWithAssertion(assertion: CheckResult.Invalid => Unit): Unit = {
      expectMatching {
        case CheckResult.Valid => fail("checker validated program but it should reject it")
        case invalid: CheckResult.Invalid => assertion(invalid)
        case CheckResult.ParsingError(e) => throw e
      }
    }

    /**
     * @param shiftedExpectedViolationsCnt
     */
    def expectInvalid(expectedViolationsCnt: Map[Int, Int]): Unit = {
      val expectedViolationsCntZeroBasedLines = expectedViolationsCnt.map(lineAndCnt => (lineAndCnt._1 - 1, lineAndCnt._2))
      expectInvalidWithAssertion { invalid =>
        val violationsWithLines = invalid.violations.map(violation => (violation.tree.pos.startLine, 1))
          .foldLeft(Map.empty[Int, Int])((acc, lineAndCnt) => {
            val (line, cnt) = lineAndCnt
            acc.updated(line, acc.getOrElse(line, default = 0) + cnt)
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

    def expectInvalid(expectedViolations: (Int, Int)*): Unit = expectInvalid(expectedViolations.toMap)

    def expectInvalidAtLines(expectedViolationLines: Int*): Unit = {
      val expectedViolationsAsMap =
        (for (line <- expectedViolationLines) yield line -> expectedViolationLines.count(_ == line)).toMap
      expectInvalid(expectedViolationsAsMap)
    }

    /**
     * @return an instance of TestRunner with the provided test execution steps
     */
    def build(): TestRunner = {
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
