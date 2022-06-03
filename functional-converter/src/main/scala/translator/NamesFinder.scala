package translator

import scala.meta.Term.Block
import scala.meta.transversers.Traverser
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

  // TODO test
  def findValsToInline(stats: List[Stat]): Set[String] = {
    val declaredBuilder = Set.newBuilder[Term.Name]
    val referencedBuilder = List.newBuilder[Term.Name]
    stats.foreach(stat =>
      new Traverser {
        override def apply(tree: Tree): Unit = tree match {
          case _: Block => /* ignore children */
          case valDefn@Defn.Val(_, List(Pat.Var(name)), _, _) => {
            declaredBuilder += name
            super.apply(valDefn)
          }
          case name: Term.Name => {
            referencedBuilder += name
            super.apply(name)
          }
          case other => super.apply(other)
        }
      }.apply(stat)
    )
    val referenced = referencedBuilder.result()
    val declared = declaredBuilder.result()
    referenced
      .filter(!declared.contains(_))
      .groupBy(_.value)
      .filter(_._2.size == 1)
      .keys
      .toSet
      .intersect(declared.map(_.value))
  }

}
