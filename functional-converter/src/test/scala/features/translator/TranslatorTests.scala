import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import features.syntactic.CheckResult
import features.syntactic.blacklist.{BlacklistChecker, PredefBlacklistRules}
import features.translator.{Reporter, Translator}

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
  def test13(): Unit = {
    val codeStr =
      """
        |def compute(x: Int): (Int, Int, Int) = {
        |
        |  val k = 2*x % 5
        |
        |  var t = 0
        |
        |  def aux(y: Int): Int = {
        |    if (y % 2 == 0) k
        |    else -k
        |  }
        |
        |  var l = 100
        |  while (l > 100){
        |    System.out.println(l)
        |
        |    val ls: List[Int] = (1 to l).toList
        |    for (z <- ls){
        |
        |      var p = 45
        |      while (p < 200){
        |        p += (p % 18) + 1
        |        t += p
        |      }
        |
        |      System.out.println(t)
        |      System.out.println(p)
        |
        |      val s = (p + z) * l - (l % 20) + aux(p-l)
        |      l = s
        |    }
        |
        |  }
        |
        |  (t, k, l)
        |}
        |val res1 = compute(9999)
        |val res2 = compute(999)
        |val res3 = compute(214)
        |List(res1, res2, res3)
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def test14(): Unit = {
    val codeStr =
      """
        |object SomeObj {
        |  def intervalsWhereAllValuesAreAtLeast(thres: Double, ls: List[Double]): List[(Int, Int)] = {
        |    var intervals: List[(Int, Int)] = Nil
        |    var currStart = -1
        |    var currIdx = 0
        |    for (d <- ls){
        |      if (d >= thres && currStart == -1){
        |        currStart = currIdx
        |      }
        |      else if (d < thres && currStart != -1){
        |        intervals = (currStart, currIdx-1) :: intervals
        |        currStart = -1
        |      }
        |      currIdx += 1
        |    }
        |    if (currIdx != -1){
        |      intervals = (currStart, currIdx-1) :: intervals
        |    }
        |    intervals.reverse
        |  }
        |}
        |
        |def main(): Unit = {
        |    val ls = List(7.41, 8.99, 77.4, 11, 28, 38.21, 95.5, 42.7, 89.0, 87.4, 32.22, 74.98, 0.8, -5, 101.45, 73.452, 31.91, 75.22)
        |    var thres = -10.0
        |    while (thres < 32){
        |      System.out.println(s"At least $thres: ${SomeObj.intervalsWhereAllValuesAreAtLeast(thres, ls)}")
        |      thres += 2.5
        |    }
        |}
        |
        |main()
        |""".stripMargin
    testRedirectedPrintOut(codeStr, _.translateMethodsIn(_).asInstanceOf[Source])
  }

  @Test
  def test15(): Unit = {
    val srcCode =
      """
        |val ls: List[Int] = (1 to 10).toList
        |var res: List[Int] = Nil
        |for (x: Int <- ls){
        |  var y = x
        |  while (y < 100){
        |    y *= 3
        |  }
        |  res = y :: res
        |}
        |res
        |""".stripMargin
    testRedirectedPrintOut(srcCode)
  }

  @Test
  def funProgFinalExam2020q8(): Unit = {
    /*
     * The code example used in this test is taken from the 2020 final exam of the functional programming class
     * (CS-210), EPFL
     * Source: https://gitlab.epfl.ch/lamp/cs210/-/blob/master/previous-exams/2020-final-exam/q8.md
     */
    val srcCode =
      """
        |def fImperative(nums: List[Int]): List[Int] = {
        |    var i = 0
        |    var j = 0
        |    var res: List[Int] = List()
        |
        |    while (i < nums.size) {
        |      var max = -1
        |      var cnt = 0
        |      j = i - 1
        |      while (j >= 0) {
        |        if(nums(j) > max) {
        |          cnt = cnt + 1
        |          max = nums(j)
        |        }
        |        j = j - 1
        |      }
        |      res = cnt :: res
        |      i = i + 1
        |    }
        |    res.reverse
        |  }
        |fImperative(List(182, 160, 180, 178))
        |""".stripMargin
    testRedirectedPrintOut(srcCode)
  }

  @Test
  def funProgFinalExam2020q10(): Unit = {
    /*
     * The code example used in this test is taken from the 2020 final exam of the functional programming class
     * (CS-210), EPFL
     * Source: https://gitlab.epfl.ch/lamp/cs210/-/blob/master/previous-exams/2020-final-exam/q10.md
     */
    val codeStr =
      """
        |def fImperative(elems: List[(Int, Int)]): List[Int] = {
        |    var i = 0
        |    var res: List[Int] = List()
        |
        |    while (i < elems.size) {
        |      var j = 0
        |      var cnt: Int = elems(i)._1
        |      while (j < cnt) {
        |        res = (elems(i)._2: Int) :: res
        |        j = j + 1
        |      }
        |      i = i + 1
        |    }
        |    res.reverse
        |}
        |fImperative(List((-1,5),(2,7),(1,2)))
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def funProgFinalExam2020q6(): Unit = {
    /*
     * The code example used in this test is taken from the 2020 final exam of the functional programming class
     * (CS-210), EPFL
     * Source: https://gitlab.epfl.ch/lamp/cs210/-/blob/master/previous-exams/2020-final-exam/q6.md
     */
    val codeStr =
      """
        |def fImperative(chars: List[Char]): List[Char] = {
        |    var i = 0
        |    var n: Int = chars.size
        |    var res: List[Char] = List()
        |
        |    while (i < n - 1 && chars(i) >= chars(i + 1)) {
        |      res = chars(i) :: res
        |      i = i + 1
        |    }
        |
        |    i = i + 1
        |
        |    while (i < n) {
        |      res = chars(i) :: res
        |      i = i + 1
        |    }
        |
        |    res.reverse
        |}
        |fImperative(List('d', 'b', 'c'))
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def funProgFinalExam2020q7(): Unit = {
    /*
     * The code example used in this test is taken from the 2020 final exam of the functional programming class
     * (CS-210), EPFL
     * Source: https://gitlab.epfl.ch/lamp/cs210/-/blob/master/previous-exams/2020-final-exam/q7.md
     */
    val codeStr =
      """
        |def fImperative(nums: List[Int]): List[(Int, Int)] = {
        |    var cnt = 1
        |    var i = 0
        |    var res: List[(Int, Int)] = List()
        |
        |    while (i < nums.length - 1) {
        |      if (nums(i) == nums(i + 1)) cnt = cnt + 1
        |      else {
        |        res = (cnt, nums(i)) :: res
        |        cnt = 1
        |      }
        |      i = i + 1
        |    }
        |    if(nums.isEmpty) Nil
        |    else ((cnt, nums(nums.length - 1)) :: res).reverse
        |}
        |fImperative(List(2,2,2,7,7,2))
        |""".stripMargin
    testRedirectedPrintOut(codeStr)
  }

  @Test
  def funProgFinalExam2020q9(): Unit = {
    /*
     * The code example used in this test is taken from the 2020 final exam of the functional programming class
     * (CS-210), EPFL
     * Source: https://gitlab.epfl.ch/lamp/cs210/-/blob/master/previous-exams/2020-final-exam/q9.md
     */
    val codeStr =
      """
        |def fImperative(l1: List[Char], l2: List[Char]): List[Char] = {
        |    var i = 0
        |    var res: List[Char] = List()
        |
        |    while (i < l1.size) {
        |      var j = 0
        |      while (j < l2.size) {
        |        if(l2(j) == l1(i)) {
        |          res = l2(j) :: res
        |          j = l2.size
        |        }
        |        j = j + 1
        |      }
        |      i = i + 1
        |    }
        |    res.reverse
        |}
        |fImperative(List('o','t','h','e','r'), List('t','a','r','t','s'))
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

  @Test
  def shouldFail3(): Unit = {
    val codeStr =
      """
        |val arg = 15
        |var callCnt = 0
        |def foo(x: Int): Int = {
        |  callCnt += 1
        |  2*x+1
        |}
        |foo(arg)
        |""".stripMargin
    testExpectingOneFailure(codeStr, "non top-level methods are not allowed if no external var is updated in their body")
  }

  private def parse(str: String): Source = dialects.Sbt1(str).parse[Source].get

  private def testExpectingOneFailure(codeStr: String, expectedErrorMsg: String): Unit = {
    val reporter = new Reporter()
    val translator = Translator(reporter)
    translator.translateTopLevelOfSource(parse(codeStr))
    assertEquals(List(expectedErrorMsg), reporter.getReportedErrors)
  }

  private def testRedirectedPrintOut(
                                      imperativeSrcCode: String,
                                      conversionMethod: (Translator, Source) => Source = _.translateTopLevelOfSource(_)
                                    ): Unit = {
    val toolBox = currentMirror.mkToolBox()

    def execute(codeStr: String): Any = {
      toolBox.eval(toolBox.parse(codeStr))
    }

    val reporter = new Reporter()
    val translator = Translator(reporter)
    val translationSource = conversionMethod(translator, parse(imperativeSrcCode))
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