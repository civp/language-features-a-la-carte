package syntactic.whitelist

import scala.meta.Tree

/**
 * Subdivision of the Scala language, allowing a specific set of constructs
 */
sealed trait Feature {

  /**
   * Checks whether a tree is allowed by this feature
   *
   * @param tree the tree to check
   * @return true iff it is allowed
   */
  def allows(tree: Tree): Boolean

}

object Feature {

  /**
   * Feature defined as a combination of other features
   * A composite feature allows a tree iff at least one of the underlying features accepts it
   *
   * @param features the features to allow
   */
  abstract class CompositeFeature(private val features: Set[Feature]) extends Feature {
    require(features != null)

    def this(features: List[Feature]) = this(features.toSet)

    def this(features: Feature*) = this(features.toSet)

    override def allows(tree: Tree): Boolean = features.exists(_.allows(tree))

  }

  /**
   * Adapter to create a feature given the partialfunction describing it
   *
   * @param checkPF this function should return true when given a tree that is allowed and not be defined on other trees
   *                (or return false on them, but this is not necessary)
   */
  abstract class AtomicFeature(checkPF: PartialFunction[Tree, Boolean]) extends Feature {
    require(checkPF != null)

    override def allows(tree: Tree): Boolean = {
      checkPF.applyOrElse(tree, (_: Tree) => false)
    }
  }

}
