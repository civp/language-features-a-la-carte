package translator

import scala.annotation.tailrec
import scala.meta.Term.Block
import scala.meta.transversers.Traverser
import scala.meta.{Defn, Pat, Source, Stat, Term, Tree}

/**
 * Utility module containing methods for inlining
 *
 * This is needed because Translator tends to create many vals, which gives code that is difficult to read
 */
object Inliner {

  /**
   * Traverses the tree, looking for scopes and performs inlining on their statements
   */
  def inlineInStatSequences(tree: Tree): Tree = {
    tree.transform {
      case Source(stats) => Source(inlineTopLevel(stats))
      case Block(stats) => Block(inlineTopLevel(stats))
    }
  }

  /**
   * Inlines x in patterns like
   * {{{
   *  val x = foo(0)
   *  x }}}
   */
  def inlineRetVal(stats: List[Stat]): List[Stat] = {
    val numStats = stats.size
    if (numStats < 2) stats
    else {
      val lastStats = stats.slice(numStats - 2, numStats)
      val transformedLastStats = lastStats match {
        case List(Defn.Val(_, List(Pat.Tuple(args1)), _, rhs), Term.Tuple(args2))
          if args1.forall(_.isInstanceOf[Pat.Var]) && args2.forall(_.isInstanceOf[Term.Name])
        =>
          if (args1.map { case Pat.Var(name) => name.value } == args2.map { case Term.Name(nameStr) => nameStr }) {
            List(rhs)
          }
          else lastStats
        case List(Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, rhs), Term.Name(nameStr2)) if nameStr1 == nameStr2 => List(rhs)
        case _ => lastStats
      }
      stats.slice(0, numStats - 2) ++ transformedLastStats
    }
  }

  /**
   * Inlines x in patterns like
   * {{{
   *   val x = 0
   *   val y = f(a, x)  }}}
   */
  @tailrec def inlineExternalCalls(stats: List[Stat]): List[Stat] = {

    val declarationsSetBuilder = Set.newBuilder[String]
    val referencesListBuilder = List.newBuilder[String]

    // collect all the declarations and uses of vals
    val traverser = new Traverser {
      @tailrec
      override def apply(tree: Tree): Unit = tree match {
        case _: Defn.Def => // ignore children (we are only scanning top-level)
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr))), _, rhs) =>
          declarationsSetBuilder += nameStr
          apply(rhs)
        case Term.Name(nameStr) => referencesListBuilder += nameStr
        case other => super.apply(other)
      }
    }
    for (stat <- stats) traverser(stat)

    val declarations = declarationsSetBuilder.result()
    val references = referencesListBuilder.result()

    // inlining candidates are the vals that are declared and then referenced only once
    val inliningCandidates = references
      .groupBy(identity)
      .filter(_._2.size == 1)
      .keySet
      .intersect(declarations)

    def mustInline(nameStr1: String, args: List[Term]): Boolean = {
      inliningCandidates.contains(nameStr1) && args.exists {
        case Term.Name(nameStr2) => nameStr2 == nameStr1
        case _ => false
      }
    }

    def argsAfterInliningDone(nameStr1: String, args: List[Term], rhs: Term): List[Term] = {
      args.map {
        case Term.Name(nameStr2) if nameStr2 == nameStr1 => rhs
        case other => other
      }
    }

    @tailrec
    def recurse(remainingStats: List[Stat], alreadyProcessed: List[Stat]): List[Stat] = {
      remainingStats match {

        /* val x = <rhs>
         * def autoGenMethod(x: Int): Int = ...
         * f(a, x)
         * ...
         * -> Inline x: f(a, x) becomes f(a, <rhs>) */
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, rhs) :: (methodDef: Defn.Def) :: (appl@Term.Apply(_, args)) :: tail
          if mustInline(nameStr1, args)
        =>
          val newArgs = argsAfterInliningDone(nameStr1, args, rhs)
          recurse(tail, alreadyProcessed ++ List(methodDef, appl.copy(args = newArgs)))

        /* val x = <rhs>
         * def autoGenMethod(x: Int): Int = ...
         * val y = f(a, x)
         * ...
         * -> Inline x: f(a, x) becomes f(a, <rhs>) */
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, rhs)
          :: (methodDef: Defn.Def)
          :: (finalValDefn@Defn.Val(_, _, _, appl@Term.Apply(_, args)))
          :: tail
          if mustInline(nameStr1, args)
        =>
          val newArgs = argsAfterInliningDone(nameStr1, args, rhs)
          recurse(tail, alreadyProcessed ++ List(methodDef, finalValDefn.copy(rhs = appl.copy(args = newArgs))))

        /* val x = <rhs>
         * f(a, x)
         * ...
         * -> Inline x: f(a, x) becomes f(a, <rhs>)   */
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, rhs) :: (appl@Term.Apply(_, args)) :: tail
          if mustInline(nameStr1, args)
        =>
          val newArgs = argsAfterInliningDone(nameStr1, args, rhs)
          recurse(tail, alreadyProcessed :+ appl.copy(args = newArgs))

        /* val x = <rhs>
         * val y = f(a, x)
         * -> Inline x: f(a, x) becomes f(a, <rhs>)    */
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, rhs)
          :: (finalValDefn@Defn.Val(_, _, _, appl@Term.Apply(_, args)))
          :: tail
          if mustInline(nameStr1, args)
        =>
          val newArgs = argsAfterInliningDone(nameStr1, args, rhs)
          recurse(tail, alreadyProcessed :+ finalValDefn.copy(rhs = appl.copy(args = newArgs)))

        case head :: tail => recurse(tail, alreadyProcessed :+ head)
        case Nil => alreadyProcessed
      }
    }

    val transformed = recurse(stats, Nil)

    // repeat as long as there are changes
    if (transformed == stats) stats
    else inlineExternalCalls(transformed)
  }

  private def inlineTopLevel(stats: List[Stat]): List[Stat] = {
    inlineRetVal(inlineExternalCalls(stats))
  }

}
