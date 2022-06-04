import syntactic.Violation
import syntactic.whitelist.{Feature, WhitelistChecker}

import scala.collection.compat.immutable.LazyList
import scala.meta.Tree

class FeaturesSetComputer(availableFeatures: List[Feature]) {

  def minimalFeaturesSetFor(tree: Tree): Option[Set[Feature]] = {
    minimalFeaturesSetFor(tree.collect { case tree => tree })
  }

  def minimalFeaturesSetToResolve(violations: List[Violation]): Option[Set[Feature]] = {
    minimalFeaturesSetFor(violations.map(_.forbiddenNode))
  }

  def minimalFeaturesSetFor(nodes: List[Tree]): Option[Set[Feature]] = {

    def search(currentlySelectedFeatures: Set[Feature]): Option[Set[Feature]] = {
      val checker = WhitelistChecker(currentlySelectedFeatures.toList)
      if (nodes.forall(checker.checkTree(_).isEmpty)){
        val oneFeatureRemovedSubsets =
          for (feature <- currentlySelectedFeatures.to[LazyList]) yield currentlySelectedFeatures - feature
        oneFeatureRemovedSubsets.map(subset => search(subset)).find(_.nonEmpty).getOrElse(Some(currentlySelectedFeatures))
      }
      else None
    }

    search(availableFeatures.toSet)
  }

}
