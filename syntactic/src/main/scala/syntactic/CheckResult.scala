package syntactic

/**
 * Result of checking a program
 */
sealed trait CheckResult[+V <: Violation]

object CheckResult {

  /**
   * syntactic_checker.CheckResult meaning that the program complies to the specified list of features
   */
  case object Valid extends CheckResult[Nothing]

  /**
   * syntactic_checker.CheckResult meaning that the program contains forbidden trees
   *
   * @param violations the forbidden trees
   */
  case class Invalid[+V <: Violation](violations: List[V]) extends CheckResult[V]

  /**
   * syntactic_checker.CheckResult meaning that the program could not be checked against the features specification
   * because of a parsing error
   *
   * @param cause the throwable thrown on the error
   */
  case class ParsingError(cause: Throwable) extends CheckResult[Nothing]
}
