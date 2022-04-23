package syntactic_checker

import scala.meta.{Dialect, Source, Tree}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try, Using}

trait Checker {

  /**
   * Check whether the input tree uses only allowed features
   *
   * @param tree the tree to be checked
   * @return Some[Violation] if a violation was found, None o.w.
   */
  def checkTree(tree: Tree): Option[Violation]

  /**
   * Check whether the input program (as a source) uses only allowed features
   *
   * @param src the source to be checked
   * @return a CheckResult (Valid, Invalid or ParsingError)
   */
  final def checkSource(src: Source): CheckResult = {
    val violations = src.collect {
      case tree: Tree => checkTree(tree)
    }.flatten
    if (violations.isEmpty) CheckResult.Valid
    else CheckResult.Invalid(violations)
  }

  /**
   * Check whether the input program (as a string) uses only allowed features
   *
   * @param dialect    the Scala dialect that the parsing should use
   * @param sourceCode the string to be checked
   * @return a CheckResult (Valid, Invalid or ParsingError)
   */
  final def checkCodeString(dialect: Dialect, sourceCode: String): CheckResult = {
    val inputWithDialect = dialect(sourceCode)
    Try {
      inputWithDialect.parse[Source].get
    } match {
      case Success(source) => checkSource(source)
      case Failure(NonFatal(throwable)) => CheckResult.ParsingError(throwable)
      case Failure(fatal) => throw fatal
    }
  }

  /**
   * Check whether the program contained in the file with the given filename uses only allowed features
   *
   * @param dialect  the Scala dialect that the parsing should use
   * @param filename the name of the file containing the program to be checked
   * @return a CheckResult (Valid, Invalid or ParsingError)
   */
  final def checkFile(dialect: Dialect, filename: String): CheckResult = {
    val content = Using(scala.io.Source.fromFile(filename)) { bufferedSource =>
      bufferedSource.getLines().mkString("\n")
    }
    content.map(checkCodeString(dialect, _)) match {
      case Failure(exception) => CheckResult.ParsingError(exception)
      case Success(checkRes) => checkRes
    }
  }

}
