package translator

import scala.collection.mutable.ListBuffer

/**
 * Container for error messages
 */
class Reporter {
  private val reportedEvents = ListBuffer[String]()

  /**
   * Report a new error
   */
  def addErrorMsg(msg: String): Unit = {
    reportedEvents.append(msg)
  }

  /**
   * @return all the errors reported until now
   */
  def getReportedErrors: List[String] = reportedEvents.toList

}
