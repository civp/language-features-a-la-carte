package features.translator

import scala.meta.{Term, Type}

/**
 * Keeps track of the vals and vars that are known at some point in the program
 */
case class NamingContext(currentlyReferencedVars: Map[String, VarInfo], currentlyReferencedVals: Map[String, Option[Type]]) {

  /**
   * Copy of this with a new var
   */
  def updatedWithVar(name: String, tpe: Type)(implicit di: DisambigIndices): NamingContext = {
    val varInfo = currentlyReferencedVars.getOrElse(name, VarInfo(name, -1, tpe))
    NamingContext(
      currentlyReferencedVars.updated(name, varInfo.copy(disambigIdx = di.getAndIncrementDisambigIdxFor(name))),
      currentlyReferencedVals
    )
  }

  /**
   * To be called when an already known variable is updated; associates the variable with a new disambiguation index
   */
  def updatedAlreadyExistingVar(name: String)(implicit di: DisambigIndices): NamingContext = {
    assert(currentlyReferencedVars.contains(name))
    updatedWithVar(name, currentlyReferencedVars(name).typ)
  }

  /**
   * @return the current disambiguated name for the given variable (e.g. may be `x_2` for a variable `x`)
   */
  def disambiguatedNameForVar(name: String): Term.Name =
    currentlyReferencedVars.get(name).map(_.toDisambiguatedName).getOrElse(Term.Name(name))

  /**
   * Copy of this with a new val
   */
  def updatedWithVal(name: String, optType: Option[Type]): NamingContext =
    copy(currentlyReferencedVals = currentlyReferencedVals.updated(name, optType))

  /**
   * @return a NamingContext containing all the vals and vars contained in either this or that
   */
  def mergedWith(that: NamingContext): NamingContext =
    NamingContext(currentlyReferencedVars ++ that.currentlyReferencedVars, currentlyReferencedVals ++ that.currentlyReferencedVals)
}

object NamingContext {
  /**
   * NamingContext with no val nor var
   */
  val empty: NamingContext = NamingContext(Map.empty, Map.empty)
}

/**
 * Associates a variable name with its disambiguation index (cf [[translator.DisambigIndices]]) and type
 */
case class VarInfo(rawName: String, disambigIdx: Int, typ: Type) {
  def toDisambiguatedName: Term.Name =
    Term.Name(rawName + (if (disambigIdx == 0) "" else s"_$disambigIdx"))
}
