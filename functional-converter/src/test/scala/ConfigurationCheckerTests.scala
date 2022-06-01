import translator.{Reporter, TranslationConfigurationChecker}
import org.junit.Test
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}

import scala.meta.{Defn, Source, dialects}


class ConfigurationCheckerTests {

  private def parse(str: String): Source = dialects.Sbt1(str).parse[Source].get

  private def headStatOf(src: Source): Defn.Def = {
    src.stats.head.asInstanceOf[Defn.Def]
  }

  @Test
  def valid_input_should_be_accepted(): Unit = {
    val src = parse(
      """
        |def foo(): Unit = {
        |  val x = 0
        |  var sum = 0
        |  while(x < 100){
        |    for (i <- 0 to x){
        |      sum += 5*i
        |    }
        |  }
        |  println(sum)
        |}
        |""".stripMargin)
    val reporter = new Reporter()
    val configChecker = new TranslationConfigurationChecker(reporter)
    assertTrue(configChecker.checkCanConvert(headStatOf(src)))
    assertTrue(reporter.getReportedErrors.isEmpty)
  }

  @Test
  def variable_shadowing_should_be_rejected(): Unit = {
    val src = parse(
      """
        |def foo(): Unit = {
        |  val x = 0
        |  var sum = 0
        |  while(x < 100){
        |    for (i <- 0 to x){
        |      val x = 5
        |      sum += x*i
        |    }
        |  }
        |  println(sum)
        |}
        |""".stripMargin)
    val reporter = new Reporter()
    val configChecker = new TranslationConfigurationChecker(reporter)
    assertFalse(configChecker.checkCanConvert(headStatOf(src)))
    val reportedErrors = reporter.getReportedErrors
    assertEquals(1, reportedErrors.size)
    val reportedError = reportedErrors.head
    assertTrue(reportedError.contains("Cannot convert method foo"))
    assertTrue(reportedError.contains("variable shadowing"))
    assertTrue(reportedError.contains("identifier x"))
  }

  @Test
  def higher_order_functions_should_be_rejected(): Unit = {
    val src = parse(
      """
        |def foo(): Unit = {
        |  val x = 0
        |  var sum = 0
        |  while(x < 100){
        |    (0 to x).foreach { _ =>
        |      sum += 5*i
        |    }
        |  }
        |  println(sum)
        |}
        |""".stripMargin)
    val reporter = new Reporter()
    val configChecker = new TranslationConfigurationChecker(reporter)
    assertFalse(configChecker.checkCanConvert(headStatOf(src)))
    val reportedErrors = reporter.getReportedErrors
    assertEquals(1, reportedErrors.size)
    val reportedError = reportedErrors.head
    assertTrue(reportedError.contains("Cannot convert method foo because of the following constructs"))
  }

}
