package translator

import scala.meta.{Defn, Pat, Stat, Term, Tree}

/**
 * Utility methods containing methods that traverse a statements list searching for vars/vals names
 */
object NamesFinder {

  /**
   * @return the identifiers of the vars that are not declared but updated in the given statements
   */
  def allModifiedVars(stats: List[Stat]): Set[String] = {
    val updatedVars = allUpdatedVars(stats)
    val declaredVars = allDeclaredVars(stats)
    updatedVars.diff(declaredVars)
  }

  /**
   * @return the identifiers of the vars that are updated in the given statements
   */
  def allUpdatedVars(stats: List[Stat]): Set[String] = {
    allNodesMatching(stats) {
      case Term.Assign(Term.Name(nameStr), _) => nameStr
      case assign@Term.Assign(_, _) => throw new AssertionError(assign.toString())
    }.toSet
  }

  /**
   * @return the identifiers of the vars that are declared in the given statements
   */
  def allDeclaredVars(stats: List[Stat]): Set[String] = {
    allNodesMatching(stats) {
      case varDef@Defn.Var(_, pats, _, _) if pats.size > 1 =>
        throw TranslatorException(s"not supported: $varDef")
      case Defn.Var(_, List(Pat.Var(Term.Name(nameStr))), _, _) => nameStr
      case defnVar: Defn.Var => throw new AssertionError(defnVar.toString())
    }.toSet
  }

  /**
   * @return all the names that appear in the given list of statements
   */
  def allReferencedNames(stats: List[Stat]): Set[String] = {
    allNodesMatching(stats) { case Term.Name(nameStr) => nameStr }.toSet
  }

  private def allNodesMatching[T](stats: List[Stat])(partFunc: PartialFunction[Tree, T]): List[T] = {
    stats.flatMap(_.collect(partFunc))
  }

}
