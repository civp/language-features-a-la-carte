package translator

import scala.meta.{Defn, Pat, Stat, Term, Tree}

object NamesFinder {

  def allModifiedVars(stats: List[Stat]): Set[String] = {
    val updatedVars = allUpdatedVars(stats)
    val declaredVars = allDeclaredVars(stats)
    updatedVars.diff(declaredVars)
  }

  def allNodesMatching[T](stats: List[Stat])(partFunc: PartialFunction[Tree, T]): List[T] = {
    stats.flatMap(_.collect(partFunc))
  }

  def allUpdatedVars(stats: List[Stat]): Set[String] = {
    allNodesMatching(stats) {
      case Term.Assign(Term.Name(nameStr), _) => nameStr
      case assign@Term.Assign(_, _) => throw UnexpectedConstructException(assign)
    }.toSet
  }

  def allDeclaredVars(stats: List[Stat]): Set[String] = {
    allNodesMatching(stats) {
      case varDef@Defn.Var(mods, pats, optType, optTerm) if pats.size > 1 =>
        throw TranslaterException(s"not supported: $varDef")
      case Defn.Var(mods, List(Pat.Var(Term.Name(nameStr))), optType, optTerm) => nameStr
      case defnVar: Defn.Var => throw UnexpectedConstructException(defnVar)
    }.toSet
  }

  def allReferencedVars(stats: List[Stat]): Set[String] = {
    allNodesMatching(stats) { case Term.Name(nameStr) => nameStr }.toSet
  }

}
