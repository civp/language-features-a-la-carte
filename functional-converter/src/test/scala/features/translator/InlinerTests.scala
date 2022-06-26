import org.junit.Assert.{assertEquals, fail}
import org.junit.Test
import features.translator.Inliner

import scala.meta.{Source, dialects}

class InlinerTests {

  @Test
  def inlineRetValTupleTest(): Unit = {
    val codeStr =
      """
        |val x = 42
        |val (y, bar) = foo(x)
        |(y, bar)
        |""".stripMargin
    val src = parse(codeStr)
    val exp = List("val x = 42", "foo(x)")
    assertEquals(exp, Inliner.inlineRetVal(src.stats).map(_.toString()))
  }

  @Test
  def inlineRetValSingleNameTest(): Unit = {
    val codeStr =
      """
        |val x = 42
        |val y = foo(x)
        |y
        |""".stripMargin
    val src = parse(codeStr)
    val exp = List("val x = 42", "foo(x)")
    assertEquals(exp, Inliner.inlineRetVal(src.stats).map(_.toString()))
  }

  @Test
  def inlineExternalCallTest(): Unit = {
    val codeStr =
      """
        |println("start")
        |val x = 42
        |val y = -5
        |def recurse(x: Int, y: Int): Unit = {
        |  if (x < 100){
        |    println(x+y)
        |    recurse(x + 1, y + 1)
        |  }
        |}
        |recurse(x, y)
        |println("End")
        |""".stripMargin
    val src = parse(codeStr)
    val exp = parse(
      """
        |println("start")
        |def recurse(x: Int, y: Int): Unit = {
        |  if (x < 100){
        |    println(x+y)
        |    recurse(x + 1, y + 1)
        |  }
        |}
        |recurse(42, -5)
        |println("End")
        |""".stripMargin
    ).stats.map(_.toString())
    assertEquals(exp, Inliner.inlineExternalCalls(src.stats).map(_.toString()))
  }

  private def parse(codeStr: String): Source = {
    dialects.Sbt1(codeStr).parse[Source].get
  }
}
