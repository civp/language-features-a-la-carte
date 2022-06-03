import org.junit.Test
import org.junit.Assert.assertEquals
import translator.NamesFinder

import scala.meta.{Source, dialects}

class NamesFinderTests {

  @Test
  def findValsToInlineTest(): Unit = {
    val codeStr =
      """
        |val x = 15
        |foo(x)
        |val y = 42
        |val z = x+y
        |bar(z)
        |""".stripMargin
    val src = dialects.Sbt1(codeStr).parse[Source].get
    val exp = Set("y", "z")
    val act = NamesFinder.findValsToInline(src.stats)
    assertEquals(exp, act)
  }

}
