import Checker._
import Feature.AtomicFeature

import scala.meta._
import scala.util.{Failure, Success, Using}

class Checker private(dialect: Dialect, allowedFeatures: List[Feature]) {
  import Checker.AlwaysAllowed
  private val allAllowedFeatures = AlwaysAllowed :: allowedFeatures

  def checkTree(tree: Tree): Option[Violation] = {
    if (allAllowedFeatures.exists(ft => ft.check(tree))) None
    else Some(Violation(tree))
  }

  def checkSource(src: Source): CheckResult = {
    val violations = src.collect((t: Tree) => checkTree(t)).flatten
    if (violations.isEmpty) CheckResult.Valid
    else CheckResult.Invalid(violations)
  }

  def checkCodeString(sourceCode: String): CheckResult = {
    try {
      val source = dialect(sourceCode).parse[Source].get
      checkSource(source)
    } catch {
      case e: Throwable => CheckResult.CompileError(e)
    }
  }

  def checkFile(filename: String): CheckResult = {
    val content = Using(scala.io.Source.fromFile(filename)) { bufferedSource =>
      bufferedSource.getLines().mkString("\n")
    }
    content.map(checkCodeString) match {
      case Failure(exception) => CheckResult.CompileError(exception)
      case Success(checkRes) => checkRes
    }
  }

}

object Checker {

  def apply(dialect: Dialect, features: List[Feature]): Checker = new Checker(dialect, features)
  def apply(dialect: Dialect, feature: Feature, features: Feature*): Checker = new Checker(dialect, feature :: features.toList)

  case class Violation private(tree: Tree, tpe: Class[_])
  object Violation {
    def apply(tree: Tree): Violation = new Violation(tree, tree.getClass)
  }

  sealed trait CheckResult
  object CheckResult {
    case object Valid extends CheckResult
    case class Invalid(val violations: List[Violation]) extends CheckResult
    case class CompileError(val cause: Throwable) extends CheckResult
  }

  private object AlwaysAllowed extends AtomicFeature({
    case _ : Source => true
    case _ : Template => true
    case _ : Term.Block => true
    case _ : Type.Name => true
    case _ : Self => true
    case _ : Name.Anonymous => true
    case _ : Init => true
    case _ : Term.EndMarker => true
  })

}
