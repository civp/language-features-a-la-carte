package converter

import converter.Reporter.{Error, Event, Info}

import scala.collection.mutable.ListBuffer

class Reporter {
  private val reportedEvents = ListBuffer[Event]()

  def addInfoMsg(msg: String): Unit = {
    reportedEvents.append(Info(msg))
  }

  def addErrorMsg(msg: String): Unit = {
    reportedEvents.append(Error(msg))
  }

  def getReportedEvents: List[Event] = reportedEvents.toList

}

object Reporter {

  trait Event {
    val msg: String
  }

  case class Info(override val msg: String) extends Event
  case class Error(override val msg: String) extends Event

}
