import scala.collection.mutable.ListBuffer

class TestController {
  private val allTestIds = ListBuffer[Int]()

  def registerTest(testId: Int): Unit = {
    allTestIds += testId
  }

  def unregisterTest(testId: Int): Unit = {
    val idx = allTestIds.indexOf(testId)
    require(idx > -1)
    allTestIds.remove(idx)
  }

  def assertEmpty(): Unit = {
    if (allTestIds.nonEmpty) throw new IllegalStateException(
      "test configuration error: some builders for TestRunner were created but run() was never called on the built object"
    )
  }

}

