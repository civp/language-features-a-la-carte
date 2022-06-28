import features.syntactic.whitelist.FeaturesProvider
import features.syntactic.whitelist.Feature.AtomicFeature
import scala.meta.Tree
import scala.meta.Type

object CustomizedFeaturesProvider extends FeaturesProvider {

  case object UnionTypes extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Type.Or => true
      case _: Type.ApplyInfix => true
    }
  }

}
