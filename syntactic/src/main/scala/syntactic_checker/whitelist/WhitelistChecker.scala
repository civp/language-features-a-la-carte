package syntactic_checker.whitelist

import syntactic_checker.whitelist.Feature.{AtomicFeature, CompositeFeature}
import syntactic_checker.whitelist.WhitelistChecker.WhitelistViolation
import syntactic_checker.{Checker, Violation}

import scala.meta.{Init, Name, Self, Source, Template, Term, Tree, Type}

/**
 * A checker to enforce the features specification
 */
class WhitelistChecker private(allowedFeatures: List[Feature]) extends Checker[WhitelistViolation] {

  import WhitelistChecker.AlwaysAllowed

  private object GroupedFeature extends CompositeFeature(AlwaysAllowed :: allowedFeatures)

  override def checkTree(tree: Tree): Option[WhitelistViolation] = {
    if (GroupedFeature.allows(tree)) None
    else Some(WhitelistViolation(tree))
  }

}

object WhitelistChecker {

  /**
   * @param features the features that are allowed in the programs to be checked
   */
  def apply(features: List[Feature]): WhitelistChecker = new WhitelistChecker(features)

  /**
   * @param features the features that are allowed in the programs to be checked
   */
  def apply(feature: Feature, features: Feature*): WhitelistChecker = new WhitelistChecker(feature :: features.toList)

  /**
   * A violation detected by the checker
   *
   * @param forbiddenNode the tree on which the violation happened
   */
  case class WhitelistViolation(override val forbiddenNode: Tree) extends Violation {
    override def toString: String = s"WhitelistViolation($forbiddenNode, ${forbiddenNode.getClass})"
  }

  // Specification of the constructs that should always be allowed
  // These constructs do not allow to write any meaningful program on their own,
  // but they arise in many Features so it is easier to always allow them
  private object AlwaysAllowed extends AtomicFeature({
    case _: Source => true
    case _: Template => true
    case _: Term.Block => true
    case _: Type.Name => true
    case _: Term.Name => true
    case _: Self => true
    case _: Term.Select => true
    case _: Name.Anonymous => true
    case _: Init => true
    case _: Term.EndMarker => true
  })

}
