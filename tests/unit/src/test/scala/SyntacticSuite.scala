package tests

import testkit.{TestFile, Specs}
import syntactic.Checker
import syntactic.CheckResult
import syntactic.Violation

import scala.meta.io.AbsolutePath
import scala.meta.Dialect

abstract class SyntacticSuite extends munit.FunSuite {

  protected def getTestFile(name: String): TestFile = {
    val path = AbsolutePath(s"../input/res/$name.scala")
    new TestFile(name, path)
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

  protected def checkFile(checker: Checker, file: TestFile, dialect: Dialect): Unit = {
    val expectedViolations = Specs.load(file, dialect)
    checker.checkFile(dialect, file.toString) match {
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
