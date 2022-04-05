import Checker.{CheckResult, Violation}
import Feature.AtomicFeature

import scala.meta.{Dialect, Init, Name, Self, Source, Template, Term, Tree, Type}
import scala.util.{Failure, Success, Using}

/**
 * A checker to enforce the features specification
 */
class Checker private(dialect: Dialect, allowedFeatures: List[Feature]) {
  import Checker.AlwaysAllowed
  private val allAllowedFeatures = AlwaysAllowed :: allowedFeatures

  /**
   * Check whether the input tree uses only allowed features
   * @param tree the tree to be checked
   * @return Some[Violation] if a violation was found, None o.w.
   */
  def checkTree(tree: Tree): Option[Violation] = {
    if (allAllowedFeatures.exists(_.check(tree))) None
    else Some(Violation(tree))
  }

  /**
   * Check whether the input program (as a source) uses only allowed features
   * @param src the source to be checked
   * @return a CheckResult (Valid, Invalid or ParsingError)
   */
  def checkSource(src: Source): CheckResult = {
    val violations = src.collect((tree: Tree) => checkTree(tree)).flatten
    if (violations.isEmpty) CheckResult.Valid
    else CheckResult.Invalid(violations)
  }

  /**
   * Check whether the input program (as a string) uses only allowed features
   * @param sourceCode the string to be checked
   * @return a CheckResult (Valid, Invalid or ParsingError)
   */
  def checkCodeString(sourceCode: String): CheckResult = {
    try {
      val source = dialect(sourceCode).parse[Source].get
      checkSource(source)
    } catch {
      case e: Throwable => CheckResult.ParsingError(e)
    }
  }

  /**
   * Check whether the program contained in the file with the given filename uses only allowed features
   * @param filename the name of the file containing the program to be checked
   * @return a CheckResult (Valid, Invalid or ParsingError)
   */
  def checkFile(filename: String): CheckResult = {
    val content = Using(scala.io.Source.fromFile(filename)) { bufferedSource =>
      bufferedSource.getLines().mkString("\n")
    }
    content.map(checkCodeString) match {
      case Failure(exception) => CheckResult.ParsingError(exception)
      case Success(checkRes) => checkRes
    }
  }

}

object Checker {

  /**
   * @param dialect the Scala dialect that the parsing should use
   * @param features the features that are allowed in the programs to be checked
   */
  def apply(dialect: Dialect, features: List[Feature]): Checker = new Checker(dialect, features)

  /**
   * @param dialect the Scala dialect that the parsing should use
   * @param features the features that are allowed in the programs to be checked
   */
  def apply(dialect: Dialect, feature: Feature, features: Feature*): Checker = new Checker(dialect, feature :: features.toList)

  /**
   * A violation detected by the checker
   * @param tree the tree on which the violation happened
   */
  case class Violation private(tree: Tree){
    override def toString: String = s"Violation($tree, ${tree.getClass})"
  }
  object Violation {
    def apply(tree: Tree): Violation = new Violation(tree)
  }

  /**
   * Result of checking a program
   */
  sealed trait CheckResult
  object CheckResult {

    /**
     * CheckResult meaning that the program complies to the specified list of features
     */
    case object Valid extends CheckResult

    /**
     * CheckResult meaning that the program contains forbidden trees
     * @param violations the forbidden trees
     */
    case class Invalid(val violations: List[Violation]) extends CheckResult

    /**
     * CheckResult meaning that the program could not be checked against the features specification
     * because of a parsing error
     * @param cause the throwable thrown on the error
     */
    case class ParsingError(val cause: Throwable) extends CheckResult
  }

  // Specification of the constructs that should always be allowed
  // These constructs do not allow to write any meaningful program on their own,
  // but they arise in many Features so it is easier to always allow them
  private object AlwaysAllowed extends AtomicFeature({
    case _ : Source => true
    case _ : Template => true
    case _ : Term.Block => true
    case _ : Type.Name => true
    case _ : Term.Name => true
    case _ : Self => true
    case _ : Term.Select => true
    case _ : Name.Anonymous => true
    case _ : Init => true
    case _ : Term.EndMarker => true
  })

}
