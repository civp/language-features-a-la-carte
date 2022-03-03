import Checker.AlwaysAllowed
import Feature.AtomicFeature

import scala.meta._

class Checker private(allowedFeatures: List[Feature]) {
  private val allAllowedFeatures = AlwaysAllowed :: allowedFeatures

  def check(src: Source): Boolean = {  // idea is to return trees that violate the rules
    src.collect((t: Tree) => check(t)).forall(identity)
  }

  def check(tree: Tree): Boolean = {
    allAllowedFeatures.exists(ft => ft.check(tree))
  }

}

object Checker {

  def apply(features: List[Feature]): Checker = new Checker(features)
  def apply(feature: Feature, features: Feature*): Checker = new Checker(feature :: features.toList)

  private object AlwaysAllowed extends AtomicFeature({
    case _ : Term.Ascribe => true
    case _ : Term.Param => true
    case _ : Term.Block => true
    case _ : Term.Placeholder => true
    case _ : Term.EndMarker => true
    case _ : Type.Placeholder => true
    case _ : Type.Bounds => true
    case _ : Type.Name => true
  })

}
