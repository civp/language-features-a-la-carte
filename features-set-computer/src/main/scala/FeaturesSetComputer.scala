import syntactic.Violation
import syntactic.whitelist.{Feature, WhitelistChecker}

import scala.collection.compat.immutable.LazyList
import scala.meta.Tree

/**
 * Given a set of available Features, computes the minimal set of Features needed by a WhitelistChecker to allow
 * a given set of language constructs
 * @param availableFeatures the features that can be included in the computed set
 */
class FeaturesSetComputer(availableFeatures: List[Feature]) {

  /**
   * Computes the minimal set of Features needed by a WhitelistChecker to allow all the nodes in the given tree
   * @param tree the tree whose nodes should be allowed
   * @return the said minimal set of Features, wrapped in a Some, if it exists, otherwise None
   */
  def minimalFeaturesSetFor(tree: Tree): Option[Set[Feature]] = {
    minimalFeaturesSetFor(tree.collect { case tree => tree })
  }

  /**
   * Assuming that a WhitelistChecker with the set of Features S1 rejected a program outputting the provided set of
   * Violations, computes the minimal set S2 of Features s.t. a WhitelistChecker with with features S1 U S2
   * will accept the program
   * @param violations the violations reported by the checker with the incomplete set of features (S1)
   * @return the set (S2) of additionally required features, wrapped in a Some, if it exists, otherwise None
   */
  def minimalFeaturesSetToResolve(violations: List[Violation]): Option[Set[Feature]] = {
    minimalFeaturesSetFor(violations.map(_.forbiddenNode))
  }

  /**
   * Computes the minimal set of Features needed by a WhitelistChecker to allow all the provided nodes
   * @param nodes the nodes to be allowed
   * @return the said minimal set of Features, wrapped in a Some, if it exists, otherwise None
   */
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
