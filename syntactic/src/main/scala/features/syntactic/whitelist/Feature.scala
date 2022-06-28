package features.syntactic.whitelist

import scala.meta.Tree

/**
 * Subdivision of the Scala language, allowing a specific set of constructs
 */
sealed trait Feature {

  /**
   * Checks whether a tree is allowed by this feature
   *
   * @param node the tree to check
   * @return true iff it is allowed
   */
  def allows(node: Tree): Boolean

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

    override def allows(node: Tree): Boolean = features.exists(_.allows(node))

    def show: String = features
      .map(_.toString)
      .filter(_ != "AlwaysAllowed") // Hide AlwaysAllowed
      .toList
      .sorted
      .mkString(", ")

  }

  /**
   * Adapter to create a feature given the partialfunction describing it
   *
   * <b>Implementations should only be singleton objects</b>
   */
  trait AtomicFeature extends Feature {

    /**
     * Returns true when given a tree that is allowed by this feature, is not defined on other trees
     * (or returns false on them, but this is not necessary)
     */
    val checkPF: PartialFunction[Tree, Boolean]

    override final def allows(node: Tree): Boolean = {
      checkPF.applyOrElse(node, (_: Tree) => false)
    }
  }

}
