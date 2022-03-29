import scala.collection.mutable

/**
 * Controller to ensure that the run method of TestRunner is called
 *
 * The purpose of this class is to avoid that some tests pass because the TestRunner was never
 * instantiated or run() was never called.
 *
 * The test class should define a TestController as a class attribute, pass it to each TestRunner that is instantiated
 * and call assertEmpty when all tests have been executed (e.g. using @AfterClass)
 */
class TestController {
  private val allTestIds = mutable.Map[Int, String]()

  /**
   * Register a TestRunner that must be executed
   */
  def registerTest(testId: Int, testName: String): Unit = {
    allTestIds.put(testId, testName)
  }

  /**
   * Unregister a TestRunner
   */
  def unregisterTest(testId: Int): Unit = {
    val prev = allTestIds.remove(testId)
    require(prev.isDefined, "erroneous testId or same test run twice")
  }

  /**
   * Checks that all registered tests have been unregistered
   */
  def assertEmpty(): Unit = {
    if (allTestIds.nonEmpty) {
      val faultyTestsNames = allTestIds.values
      object TestConfigException extends Exception(
        s"the following tests were never run after TestRunner builder instantiation: ${faultyTestsNames.mkString("\n", "\n", "\n")}" +
          s"These tests may have passed, but that is only because no check was made in them"
      )
      throw TestConfigException
    }
  }

}

