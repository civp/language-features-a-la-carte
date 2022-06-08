package tests

import semantic.Checker
import semantic.Violation
import semantic.Method
import semantic.Rules._
import testkit.{Specs, TestFile, Report}

import scala.meta.io.AbsolutePath
import scala.meta.inputs.Position

import scala.util.Properties
import tastyquery.jdk.ClasspathLoaders
import tastyquery.Contexts
import tastyquery.Contexts.BaseContext
import tastyquery.ast.Trees._
import tastyquery.ast.Types._
import tastyquery.ast.Symbols._
import tastyquery.ast.Spans._
import tastyquery.ast.Names._

class SemanticSuite extends munit.FunSuite {

  // set up base context
  private val cpElem = System.getProperty("tests-input-product")

  private val classpath =
    ClasspathLoaders.read(List(cpElem), Set(ClasspathLoaders.FileKind.Tasty))

  given BaseContext = Contexts.init(classpath)

  protected def getTestFile(name: String): TestFile = {
    val path = AbsolutePath(s"${System.getProperty("tests-input-source")}/$name.scala")
    new TestFile(name, path)
  }

  private def mergeViolations(violations: List[Violation]): List[Violation] = {

    // simple merge without taking into account lines
    def merge(vs: List[Violation], acc: List[Violation]): List[Violation] = 
      vs match {
        case va :: vb :: tail if va.span.contains(vb.span) =>
          merge(va :: tail, acc)
        case va :: vb :: tail =>
          merge(vb :: tail, va :: acc)
        case va :: Nil =>
          va :: acc
        case Nil => acc
      }

    merge(violations, List.empty).reverse
  }

  private def checkClass(checker: Checker, className: String, file: TestFile): Unit = {
    val expectedViolations = Specs.load(file)
    val violations = checker.checkClass(className)
    val derivedViolations = mergeViolations(violations).collect {
      case violation if violation.hasSpan =>
        val span = violation.span
        val pos = file.spanToPosition(span.start, span.end)
        Report.format(pos.startLine, pos.startColumn, violation.msg)
    }
    assertEquals(derivedViolations, expectedViolations)
  }

  test("no-list-head") {
    val checker = Checker(NoListHead)
    val className = "res.ListHead"
    val file = getTestFile("semantic/ListHead")
    checkClass(checker, className, file)
  }

  test("no-option-get") {
    val checker = Checker(NoOptionGet)
    val className = "res.OptionGet"
    val file = getTestFile("semantic/OptionGet")
    checkClass(checker, className, file)
  }

  test("no-println") {
    val checker = Checker(NoPrintln)
    val className = "res.Println"
    val file = getTestFile("semantic/Println")
    checkClass(checker, className, file)
  }

  test("my-println") {
    val checker = Checker(NoPrintln)
    val className = "res.MyPrintln"
    val file = getTestFile("semantic/MyPrintln")
    checkClass(checker, className, file)
  }

  test("no-foreach") {
    val checker = Checker(NoIterableOnceOpsForeach)
    val className = "res.Foreach"
    val file = getTestFile("semantic/Foreach")
    checkClass(checker, className, file)
  }

}
