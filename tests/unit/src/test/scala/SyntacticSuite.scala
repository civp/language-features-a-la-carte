package tests

import testkit.{TestPath, Specs}

import scala.meta.io.AbsolutePath
import scala.meta.Dialect
import scala.meta.dialects.Scala213

import syntactic.Checker
import syntactic.CheckResult

import scala.util.Success
import scala.util.Failure
import scala.meta.internal.semanticdb.Scala

abstract class SyntacticSuite extends munit.FunSuite {

  protected def getTestPath(name: String): TestPath = {
    val path = AbsolutePath(s"${System.getProperty("tests-input")}/$name.scala")
    new TestPath(name, path)
  }

  protected def checkPath(checker: Checker, path: TestPath, dialect: Dialect = Scala213): Unit = {
    val expectedViolations = Specs.fromPath(path)
    checker.checkFile(dialect, path.toString) match {
      case CheckResult.ParsingError(e) => throw e
      case CheckResult.Valid => fail("should not be valid")
      case CheckResult.Invalid(violations) =>
        assertEquals(violations.map(v => s"${v.startLine}:${v.startColumn}: ${v.msg}"), expectedViolations)
    }
  }
  
}
