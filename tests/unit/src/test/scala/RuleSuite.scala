import testkit.{TestPath, Specs}

import scala.meta.io.AbsolutePath

// TODO: generalize to unified checkers
import syntactic.Checker
import syntactic.Rule._

import scala.util.Success
import scala.util.Failure

class RuleSuite extends munit.FunSuite {

  private def getTestPath(name: String): TestPath = {
    val path = AbsolutePath(s"${System.getProperty("tests-input")}/$name.scala")
    new TestPath(name, path)
  }

  private def checkPath(checker: Checker, path: TestPath) = {
    val expectedViolations = Specs.fromPath(path)
    checker.checkFile(path.toString) match {
      case Success(violations) => 
        assertEquals(violations.map(_.toString), expectedViolations)
      case Failure(exception) =>
        throw exception
    }
  }
  
  test("no-null-no-cast") {
    val path = getTestPath("example/NoNullNoCast")
    // TODO: generalize to unified checkers
    val checker = Checker(NoNull, NoCast)
    checkPath(checker, path)
  }

  test("no-var-no-while") {
    val path = getTestPath("example/NoVarNoWhile")
    val checker = Checker(NoVar, NoWhile)
    checkPath(checker, path)
  }
}
