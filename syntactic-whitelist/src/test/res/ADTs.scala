
sealed trait Tree {
  case class Leaf(value: Int) extends Tree;
  case class Fork(right: Tree, left: Tree) extends Tree
}
