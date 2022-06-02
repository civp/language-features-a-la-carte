package translator

import scala.meta.{Term, Type}

case class NamingContext(currentlyReferencedVars: Map[String, VarInfo], currentlyReferencedVals: Map[String, Option[Type]]) {
  def updatedWithVar(name: String, tpe: Type)(implicit di: DisambigIndices): NamingContext = {
    val varInfo = currentlyReferencedVars.getOrElse(name, VarInfo(name, -1, tpe))
    NamingContext(
      currentlyReferencedVars.updated(name, varInfo.copy(disambigIdx = di.incAndGetDisambigIdxFor(name))),
      currentlyReferencedVals
    )
  }

  def updatedAlreadyExistingVar(name: String)(implicit di: DisambigIndices): NamingContext = {
    updatedWithVar(name, currentlyReferencedVars(name).typ)
  }

  def disambiguatedNameForVar(name: String): Term.Name =
    currentlyReferencedVars.get(name).map(_.toDisambiguatedName).getOrElse(Term.Name(name))

  def updatedWithVal(name: String, optType: Option[Type]): NamingContext =
    copy(currentlyReferencedVals = currentlyReferencedVals.updated(name, optType))

  def mergedWith(that: NamingContext): NamingContext =
    NamingContext(currentlyReferencedVars ++ that.currentlyReferencedVars, currentlyReferencedVals ++ that.currentlyReferencedVals)
}

object NamingContext {
  val empty: NamingContext = NamingContext(Map.empty, Map.empty)
}

case class VarInfo(rawName: String, disambigIdx: Int, typ: Type) {
  def toDisambiguatedName: Term.Name =
    Term.Name(rawName + (if (disambigIdx == 0) "" else s"_$disambigIdx"))
}
