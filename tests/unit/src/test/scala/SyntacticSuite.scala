package tests

import testkit.{TestPath, Specs}
import syntactic.Checker
import syntactic.CheckResult
import syntactic.Violation

import scala.meta.io.AbsolutePath
import scala.meta.Dialect

abstract class SyntacticSuite extends munit.FunSuite {

  protected def getTestPath(name: String): TestPath = {
    val path = AbsolutePath(s"${System.getProperty("tests-input")}/$name")
    new TestPath(name, path)
  }

  private def mergeViolations(violations: List[Violation]): List[Violation] = {

    def merge(vs: List[Violation], acc: List[Violation]): List[Violation] = 
      vs match {
        case va :: vb :: tail if
            va.endLine > vb.endLine ||
            va.endLine == vb.endLine &&
            va.endColumn >= vb.endColumn =>
          merge(va :: tail, acc)
        case va :: vb :: tail =>
          merge(vb :: tail, va :: acc)
        case va :: Nil =>
          va :: acc
        case Nil => acc
      }

    violations
      .groupBy(v => v.startLine)
      .flatMap {
        case (_, vs) =>
          merge(vs.sortBy(_.startColumn), List.empty)
      }
      .toList
      .sortBy(v => (v.startLine, v.startColumn))
  }

  protected def checkPath(checker: Checker, path: TestPath, dialect: Dialect): Unit = {
    val expectedViolations = Specs.fromPath(path, dialect)
    checker.checkFile(dialect, path.toString) match {
      case CheckResult.ParsingError(e) => throw e
      case CheckResult.Valid =>
        assertEquals(List.empty[String], expectedViolations)
      case CheckResult.Invalid(violations) =>
        assertEquals(
          mergeViolations(violations).map(v => s"${v.startLine}:${v.startColumn}: ${v.msg}"),
          expectedViolations
        )
    }
  }
  
}
