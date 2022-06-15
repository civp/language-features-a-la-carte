import syntactic.{CheckResult, Violation}
import syntactic.blacklist.{BlacklistChecker, PredefBlacklistRules}

import scala.meta.dialects

object CheckDemo {

  def main(args: Array[String]): Unit = {

    val path = "demo-valentin/src/main/examples/Example.scala"

    val checker = BlacklistChecker(PredefBlacklistRules.NoVar, PredefBlacklistRules.NoWhile)
    val checkRes = checker.checkFile(dialect = dialects.Scala213, path)

    checkRes match {
      case CheckResult.Valid => println("All good!")
      case CheckResult.Invalid(violations) =>
        println("Violations found:")
        violations.foreach {
          case violation @ Violation(forbiddenNode, msg) =>
            println(s"at ${violation.startLine}:${violation.startColumn}: ${forbiddenNode.getClass.getSimpleName}, $msg")
        }
      case CheckResult.ParsingError(cause) =>
        println(s"Parsing failed: $cause")
    }

  }

}
