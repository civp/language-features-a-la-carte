import syntactic.Violation
import syntactic.whitelist.{Feature, WhitelistChecker}

import scala.annotation.tailrec
import scala.meta.Tree

/**
 * Given a set of available Features, computes the minimal set of Features needed by a WhitelistChecker to allow
 * a given set of language constructs
 *
 * @param availableFeatures the features that can be included in the computed set
 */
class FeaturesSetComputer(availableFeatures: List[Feature]) {

  /**
   * Computes the minimal set of Features needed by a WhitelistChecker to allow all the nodes in the given tree
   *
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
   *
   * @param violations the violations reported by the checker with the incomplete set of features (S1)
   * @return the set (S2) of additionally required features, wrapped in a Some, if it exists, otherwise None
   */
  def minimalFeaturesSetToResolve(violations: List[Violation]): Option[Set[Feature]] = {
    minimalFeaturesSetFor(violations.map(_.forbiddenNode))
  }

  /**
   * Computes the minimal set of Features needed by a WhitelistChecker to allow all the provided nodes
   *
   * @param nodes the nodes to be allowed
   * @return the said minimal set of Features, wrapped in a Some, if it exists, otherwise None
   */
  def minimalFeaturesSetFor(nodes: List[Tree]): Option[Set[Feature]] = {

    val availableFeaturesCnt = availableFeatures.size
    val authorizations = nodes.map(_ => new Array[Boolean](availableFeaturesCnt))
    for ((node, nIdx) <- nodes.zipWithIndex) {
      for ((feature, fIdx) <- availableFeatures.zipWithIndex) {
        authorizations(nIdx)(fIdx) = WhitelistChecker(feature).checkTree(node).isEmpty
      }
    }

    def allows(featIdx: Int, nodeIdx: Int): Boolean = authorizations(nodeIdx)(featIdx)

    case class SearchState(usedFeatures: Set[Int], remNodes: Set[Int]) {
      def featureAdded(featIdx: Int): SearchState = {
        SearchState(usedFeatures + featIdx, remNodes.filter(!allows(featIdx, _)))
      }

      override def toString: String = {
        s"SearchState(${usedFeatures.map(availableFeatures(_))}, $remNodes)"
      }
    }

    @tailrec
    def search(currentAttempts: List[SearchState], currentFeatureIdx: Int): Option[Set[Int]] = {
      if (currentFeatureIdx < availableFeaturesCnt) {
        val newAttempts = currentAttempts.flatMap(attemptWithoutCurrFeat => {
          val attemptWithCurrFeat = attemptWithoutCurrFeat.featureAdded(currentFeatureIdx)
          if (attemptWithCurrFeat.remNodes.size < attemptWithoutCurrFeat.remNodes.size) {
            List(attemptWithoutCurrFeat, attemptWithCurrFeat)
          }
          else List(attemptWithoutCurrFeat)
        })
        search(newAttempts, currentFeatureIdx + 1)
      }
      else {
        val possibilities = currentAttempts.filter(_.remNodes.isEmpty)
        if (possibilities.isEmpty) None
        else Some(possibilities.map(_.usedFeatures).minBy(_.size))
      }
    }

    val selectedFeatIndicesOpt = search(List(SearchState(Set.empty, nodes.indices.toSet)), 0)
    selectedFeatIndicesOpt.map(selectedFeatIndices =>
      availableFeatures
        .zipWithIndex
        .filter(ftAndIdx => selectedFeatIndices.contains(ftAndIdx._2))
        .map(_._1)
        .toSet
    )
  }

}
