import org.junit.Assert.assertEquals
import org.junit.Test
import syntactic.CheckResult
import syntactic.blacklist.{BlacklistChecker, BlacklistRules}
import translator.{Reporter, Translator}

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.meta.{Source, dialects}
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

class TranslatorTests {

  @Test
  def test1(): Unit = {
    val imperativeSrcCode =
      """
        |var x = 0
        |var y = 0
        |while (x < 10){
        |  x += 1
        |  y += x
        |  System.out.print(x + ",")
        |  x += 1
        |  y %= 100
        |  System.out.print(y + " ; ")
        |}
        |System.out.flush()
        |x += y
        |x
        |""".stripMargin

    testRedirectedPrintOut(imperativeSrcCode)
  }

  @Test
  def test2(): Unit = {
    val imperativeSourceCode =
      """
        |var x = 1
        |var foo = true
        |var nbIter = 0
        |var bar = 15
        |while (foo){
        |  x -= x*5
        |  foo = (x % 7) != 0
        |  nbIter += 1
        |  System.out.println(foo)
        |  System.out.println(s"$nbIter iterations")
        |}
        |foo = true
        |if (foo) 10 + x else x-bar
        |""".stripMargin
    testRedirectedPrintOut(imperativeSourceCode)
  }

  @Test
  def test3(): Unit = {
    val sourceCode =
      """
        |def fibonacci(n: Int): Int = {
        |  require(n >= 0)
        |  if (n <= 1){
        |    return n
        |  }
        |  var i = 2
        |  var oldest = 0
        |  var old = 1
        |  while (i <= n){
        |    val curr = oldest + old
        |    oldest = old
        |    old = curr
        |    i += 1
        |  }
        |  old
        |}
        |fibonacci(11)
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  @Test
  def test4(): Unit = {
    val sourceCode =
      """
        |def abs(u: Int): Int = if (u >= 0) u else -u
        |var x = 20
        |var y = 0
        |var foo = true
        |while (x > -5){
        |  if (foo){
        |    x -= abs(y)
        |    y += 1
        |  } else {
        |    x -= 1
        |  }
        |  foo = ((x % y) % 2 == 0)
        |  println(foo)
        |}
        |(x, y)
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  @Test
  def test5(): Unit = {
    val sourceCode =
      """
        |var i = 1
        |while (i < 10){
        |  var j = 2
        |  while (j < 11){
        |    System.out.print(s"$i $j / ")
        |    j += 1
        |  }
        |  i += 1
        |}
        |System.out.flush()
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  @Test
  def test6(): Unit = {
    val sourceCode =
      """
        |def scalarProd(u: List[Int], v: List[Int]): Int = {
        |  require(u.size == v.size)
        |  var sum = 0
        |  val zipped: List[(Int, Int)] = u.zip(v)
        |  for ((un, vn) <- zipped){
        |    sum += un * vn
        |  }
        |  return sum
        |}
        |scalarProd(List(-1, 3, 2), List(2, -2, 1))
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  private def testRedirectedPrintOut(imperativeSrcCode: String): Unit = {
    val toolBox = currentMirror.mkToolBox()

    def execute(codeStr: String): Any = {
      toolBox.eval(toolBox.parse(codeStr))
    }

    def parse(str: String): Source = dialects.Sbt1(str).parse[Source].get

    val reporter = new Reporter()
    val translator = Translator(reporter)
    val translationSource = translator.translateSource(parse(imperativeSrcCode))
    val translationStr = translationSource.toString()

    println("----------------------------------------------------------")
    println(translationStr)
    println("----------------------------------------------------------")

    val checker = BlacklistChecker(BlacklistRules.NoVar, BlacklistRules.NoWhile)
    assertEquals(CheckResult.Valid, checker.checkTree(translationSource))

    val imperativeResultBuffer = new ByteArrayOutputStream()
    val functionalResultBuffer = new ByteArrayOutputStream()
    val saved = System.out
    try {
      System.setOut(new PrintStream(imperativeResultBuffer))
      val imperativeRet = execute(imperativeSrcCode)
      println(s"Imperative returned: $imperativeRet")
      System.setOut(new PrintStream(functionalResultBuffer))
      val functionalRet = execute(translationStr)
      println(s"Functional returned: $functionalRet")
      assertEquals(imperativeRet, functionalRet)
    } finally {
      System.setOut(saved)
    }
    val imperativeResult = imperativeResultBuffer.toString
    val functionalResult = functionalResultBuffer.toString
    assertEquals(imperativeResult, functionalResult)
    println(s"Imperative printed: $imperativeResult")
    println(s"Functional printed: $functionalResult")
  }
}