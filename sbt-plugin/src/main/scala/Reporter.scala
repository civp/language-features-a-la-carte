package sbtlanguagefeatures

import syntactic.CheckResult

import java.io.File
import java.nio.file.{Files, Paths}

private[sbtlanguagefeatures] object Reporter {

  def report(file: File, result: CheckResult): Unit = {
    val path = file.getAbsolutePath()
    val lines = Files.readAllLines(Paths.get(path))
    result match {
      case CheckResult.ParsingError(e) =>
        println(e.getMessage().replaceFirst("<input>", path))
      case CheckResult.Valid =>
      case CheckResult.Invalid(violations) =>
        violations
          .map { v =>
            s"$path:${v.startLine + 1}: ${v.msg}\n" +
            s"${lines.get(v.startLine)}\n" +
            " " * v.startColumn + "^"
          }
          .distinct
          .foreach(println)
    }
  }

}
