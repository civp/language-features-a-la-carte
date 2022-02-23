import scala.meta._

class Checker private(allowedFeatures: List[Feature]) {

  def check(src: Source): Boolean = {  // idea is to return trees that violate the rules
    src.collect((t: Tree) => check(t)).forall(identity)
  }

  def check(tree: Tree): Boolean = {
    allowedFeatures.exists(ft => ft.check(tree))
  }

}

object Checker {

  def apply(features: List[Feature]): Checker = new Checker(features)
  def apply(feature: Feature, features: Feature*): Checker = new Checker(feature :: features.toList)

}
