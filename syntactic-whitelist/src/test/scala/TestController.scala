import scala.collection.mutable

class TestController {
  private val allTestIds = mutable.Map[Int, String]()

  def registerTest(testId: Int, testName: String): Unit = {
    allTestIds.put(testId, testName)
  }

  def unregisterTest(testId: Int): Unit = {
    val prev = allTestIds.remove(testId)
    require(prev.isDefined)
  }

  def assertEmpty(): Unit = {
    if (allTestIds.nonEmpty) {
      val faultyTestsNames = allTestIds.values
      object TestConfigException extends Exception(
        s"the following test(s) never called run() after TestRunner builder instantiation: ${faultyTestsNames.mkString("\n", "\n", "\n")}"
      )
      throw TestConfigException
    }
  }

}

