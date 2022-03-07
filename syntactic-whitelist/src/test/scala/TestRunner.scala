import Checker.CheckResult
import org.junit.Assert.assertEquals

import java.util.concurrent.atomic.AtomicInteger

/**
 * Test runner for whitelist checker tests
 */
class TestRunner private(matcher: CheckResult => Unit, filename: String, features: List[Feature],
                         testController: TestController, uid: Int) {
  import TestRunner._

  /**
   * Execute the tests
   */
  def run(): Unit = {
    testController.unregisterTest(uid)
    val checker = Checker(features)
    val actualRes = checker.checkFile(s"$TEST_RES_DIR/$filename.$TEST_FILES_EXT")
    matcher(actualRes)
  }

}

object TestRunner {

  private val uidGenerator = new AtomicInteger(0)

  private val TEST_RES_DIR = "src/test/res"
  private val TEST_FILES_EXT = "scala"

  class Builder(testController: TestController) {
    private var matcher: Option[CheckResult => Unit] = None
    private var filename: Option[String] = None
    private var features: Option[List[Feature]] = None
    private val uid = uidGenerator.incrementAndGet()

    private val TEST_METHOD_FRAME_IDX = 1

    {
      val testName = StackWalker.getInstance().walk(frames => {
        val frame = frames.limit(TEST_METHOD_FRAME_IDX+1).toList.get(TEST_METHOD_FRAME_IDX)
        frame.getMethodName
      })
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
     * @param matcher function that will be called on the resulting CheckResult, in order to check its correctness
     */
    def expectingMatching(matcher: CheckResult => Unit): Builder = {
      if (this.matcher.isDefined){
        throw new IllegalStateException("matcher set more than once")
      }
      this.matcher = Some(matcher)
      this
    }

    /**
     * @param expected expected test output, will be compared to the actual one using assertEquals
     */
    def expectingResult(expected: CheckResult): Builder = {
      if (this.matcher.isDefined){
        throw new IllegalStateException("matcher set more than once")
      }
      this.matcher = Some(
        (actual: CheckResult) => assertEquals(expected, actual)
      )
      this
    }

    /**
     * @param _features the features that the Checker should allow
     */
    def withFeatures(_features: Feature*): Builder = {
      if (this.features.isDefined){
        throw new IllegalStateException("features set more than once")
      }
      this.features = Some(_features.toList)
      this
    }

    /**
     * @return an instance of TestRunner with the provided test execution steps
     */
    def build(): TestRunner = {
      if (filename.isEmpty) throw new IllegalStateException("cannot build TestRunner: no filename")
      if (matcher.isEmpty) throw new IllegalStateException("cannot build TestRunner: no expectedRes")
      if (features.isEmpty) throw new IllegalStateException("cannot build TestRunner: no features")
      new TestRunner(matcher.get, filename.get, features.get, testController, uid)
    }

  }

}
