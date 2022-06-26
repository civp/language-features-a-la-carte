import syntactic.whitelist.FeaturesProvider
import syntactic.whitelist.Feature.AtomicFeature
import scala.meta.Type

object CustomizedFeaturesProvider extends FeaturesProvider {

  case object UnionTypes extends AtomicFeature({
    case _: Type.Or => true
    case _: Type.ApplyInfix => true
  })

}
