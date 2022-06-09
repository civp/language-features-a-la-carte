import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import syntactic.CheckResult
import syntactic.blacklist.{BlacklistChecker, PredefBlacklistRules}
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
        |    n
        |  } else {
        |    var i = 2
        |    var oldest = 0
        |    var old = 1
        |    while (i <= n){
        |      val curr = oldest + old
        |      oldest = old
        |      old = curr
        |      i += 1
        |    }
        |    old
        |  }
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
        |  sum
        |}
        |scalarProd(List(-1, 3, 2), List(2, -2, 1))
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  @Test
  def test7(): Unit = {
    val sourceCode =
      """
        |val ls: List[Either[Int, String]] = List(Left(15), Right("Hello"), Right("World"), Left(-1))
        |var x = 0
        |for (e <- ls){
        |  e match {
        |    case Left(i) =>
        |      x += i
        |      System.out.println(s"$i, $x")
        |    case Right(str) =>
        |      x += str.size
        |      System.out.println(s"[$x]")
        |  }
        |}
        |println(x)
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  @Test
  def test8(): Unit = {
    val sourceCode =
      """
        |var x = 5
        |while (x > 0){
        |  case class Foo(bar: Int)
        |  val f = new Foo(2)
        |  x -= f.bar
        |}
        |println(x)
        |""".stripMargin
    testRedirectedPrintOut(sourceCode)
  }

  @Test
  def test9(): Unit = {
    val codeStr =
      """def scalarProd(u: List[Int], v: List[Int]): Int = {
        |  require(u.size == v.size)
        |  var sum = 0
        |  val zipped: List[(Int, Int)] = u.zip(v)
        |  for ((un, vn) <- zipped){
        |    sum += un * vn
        |  }
        |  sum
        |}
        |def computation(): Int = {
        |  val vecLs: List[(List[Int], List[Int])] = List(
        |    (List(2, 5, -1), List(3, -11, 7)),
        |    (List(-2, -1, 1), List(4, 3)),
        |    (List(2, 7), List(5, 10))
        |  )
        |  var sum2 = 0
        |  for ((u, v) <- vecLs if u.size == v.size){
        |    val currProd = scalarProd(u, v)
        |    sum2 += currProd
        |    System.out.println(currProd)
        |  }
        |  sum2
        |}
        |computation()
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def test10(): Unit = {
    val codeStr =
      """
        |val ls: List[Int] = (1 to 50).toList
        |val ls2 = for (i <- ls) yield {
        |  var s = 0
        |  s += i
        |  s += i
        |  s
        |}
        |println(ls2)
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def test11(): Unit = {
    val codeStr =
      """
        |var x = -15
        |var y = 20
        |var alternate = false
        |do {
        |  if (alternate) x += 1
        |  else y -= 1
        |  alternate = !alternate
        |  System.out.println(s"$alternate, $x, $y")
        |} while (x < y)
        |x
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def test12(): Unit = {
    val codeStr =
      """
        |var x = 15
        |var y = 10
        |var alternate = false
        |do {
        |  if (alternate) x += 1
        |  else y -= 1
        |  alternate = !alternate
        |  System.out.println(s"$alternate, $x, $y")
        |} while (x < y)
        |x
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def shouldFail1(): Unit = {
    val codeStr =
      """
        |val ls: List[Int] = (1 to 50).toList
        |var s = 0
        |val ls2 = for (i <- ls) yield {
        |  s += i
        |  i
        |}
        |println(ls2)
        |println(s)
        |""".stripMargin
    testExpectingOneFailure(codeStr, "for-yield expressions are only supported if no external var is updated in their body")
  }

  @Test
  def shouldFail2(): Unit = {
    val codeStr =
      """
        |val ls: List[Int] = (1 to 50).toList
        |var s = 0
        |ls.foreach { i =>
        |  s += i
        |}
        |println(s)
        |""".stripMargin
    testExpectingOneFailure(codeStr, "higher-order functions are only supported if no external var is updated in their body")
  }

  private def parse(str: String): Source = dialects.Sbt1(str).parse[Source].get

  private def testExpectingOneFailure(codeStr: String, expectedErrorMsg: String): Unit = {
    val reporter = new Reporter()
    val translator = Translator(reporter)
    translator.translateTopLevelOfSource(parse(codeStr))
    assertEquals(List(expectedErrorMsg), reporter.getReportedErrors)
  }

  private def testRedirectedPrintOut(imperativeSrcCode: String): Unit = {
    val toolBox = currentMirror.mkToolBox()

    def execute(codeStr: String): Any = {
      toolBox.eval(toolBox.parse(codeStr))
    }

    val reporter = new Reporter()
    val translator = Translator(reporter)
    val translationSource = translator.translateTopLevelOfSource(parse(imperativeSrcCode))
    val translationStr = translationSource.toString()

    println("----------------------------------------------------------")
    println(translationStr)
    println("----------------------------------------------------------")

    reporter.getReportedErrors.foreach(println)
    assertTrue(reporter.getReportedErrors.isEmpty)

    val checker = BlacklistChecker(PredefBlacklistRules.NoVar, PredefBlacklistRules.NoWhile)
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