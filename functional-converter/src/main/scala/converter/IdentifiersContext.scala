package converter

import scala.meta.Type

case class DisambiguatedName(originalName: String, disambiguateIndex: Int) {
  def toVarName: String = if (disambiguateIndex == 0) originalName else s"$originalName$disambiguateIndex"
}

case class IdentifiersContext(
                               nextDisambiguateIdxPerVarName: Map[String, Int],
                               valOrVarNamesToTypes: Map[String, Type]
                             ) {



}
