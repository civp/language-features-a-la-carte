package features.syntactic.whitelist

import features.syntactic.whitelist.Feature.{AtomicFeature, CompositeFeature}
import features.syntactic.{Checker, Violation}

import scala.meta.{Init, Name, Pat, Self, Source, Template, Term, Tree, Type}

/**
 * A checker to enforce the features specification
 */
class WhitelistChecker private(allowedFeatures: List[Feature]) extends Checker {

  import WhitelistChecker.AlwaysAllowed

  private object GroupedFeature extends CompositeFeature(AlwaysAllowed :: allowedFeatures)

  override def checkNode(node: Tree): List[Violation] = {
    if (GroupedFeature.allows(node)) Nil
    else Violation(node, s"not in the allowed features: ${GroupedFeature.show}").toSingletonList
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

  // Specification of the constructs that should always be allowed
  // These constructs do not allow to write any meaningful program on their own,
  // but they arise in many Features so it is easier to always allow them
  private object AlwaysAllowed extends AtomicFeature {

    override val checkPF: PartialFunction[Tree, Boolean] = {
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
      case _: Pat.Var => true
    }

    override def toString: String = "AlwaysAllowed"
  }

}
