package features.sbtplugin

import features.syntactic.CheckResult

import java.io.File
import java.nio.file.{Files, Paths}

private[sbtplugin] object Reporter {

  def format(file: File, result: CheckResult): List[String] = {
    val path = file.getAbsolutePath()
    val lines = Files.readAllLines(Paths.get(path))
    result match {
      case CheckResult.ParsingError(e) =>
        e.getMessage().replaceFirst("<input>", path) :: Nil
      case CheckResult.Valid => List.empty
      case CheckResult.Invalid(violations) =>
        violations
          .map { v =>
            s"$path:${v.startLine + 1}: ${v.msg}\n" +
              s"${lines.get(v.startLine)}\n" +
              " " * v.startColumn + "^"
          }
          .distinct
    }
  }

}
