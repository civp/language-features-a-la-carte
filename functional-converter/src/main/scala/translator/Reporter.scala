package translator

import scala.collection.mutable.ListBuffer

class Reporter {
  private val reportedEvents = ListBuffer[String]()

  def addErrorMsg(msg: String): Unit = {
    reportedEvents.append(msg)
  }

  def getReportedErrors: List[String] = reportedEvents.toList

}