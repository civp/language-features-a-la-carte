package sbtlanguagefeatures

import syntactic.{CheckResult, Violation}
import java.io.File

object Reporter {

  def report(file: File, result: CheckResult): Unit = {
    print(s"${file.getAbsolutePath()}: ")
    result match {
      case CheckResult.ParsingError(e) => println(e)
      case CheckResult.Valid => println("valid")
      case CheckResult.Invalid(violations) =>
        println(s"${violations.length} violation(s)")
        violations
          .map(v => s"${v.startLine + 1}:${v.startColumn + 1} => ${v.msg}")
          .foreach(println)
    }
  }

}
