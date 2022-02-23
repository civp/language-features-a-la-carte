import scala.meta.Tree

trait Feature {

  def check(tree: Tree): Boolean

}

object Feature {

  case class CompositeFeature(features: Feature*) extends Feature {
    override def check(tree: Tree): Boolean = features.exists(_.check(tree))
  }

  abstract class AtomicFeature(checkPF: PartialFunction[Tree, Boolean]) extends Feature {
    override def check(tree: Tree): Boolean = checkPF.applyOrElse(tree, (_: Tree) => false)
  }

}
