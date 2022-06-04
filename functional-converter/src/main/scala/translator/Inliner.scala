package translator

import scala.annotation.tailrec
import scala.meta.Term.Block
import scala.meta.transversers.Traverser
import scala.meta.{Defn, Pat, Source, Stat, Term, Tree}

object Inliner {

  def inlineTopLevel(stats: List[Stat]): List[Stat] = {
    inlineExternalCalls(inlineRetVal(stats))
  }

  def inlineInStatSequences(tree: Tree): Tree = {
    tree.transform {
      case Source(stats) => Source(inlineTopLevel(stats))
      case Block(stats) => Block(inlineTopLevel(stats))
    }
  }

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

  @tailrec
  def inlineExternalCalls(stats: List[Stat]): List[Stat] = {

    val declBuilder = Set.newBuilder[String]
    val refBuilder = List.newBuilder[String]

    val traverser = new Traverser {
      override def apply(tree: Tree): Unit = tree match {
        case _: Defn.Def => // ignore children
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr))), _, rhs) =>
          declBuilder += nameStr
        case Term.Name(nameStr) => refBuilder += nameStr
        case other => super.apply(other)
      }
    }
    for (stat <- stats) traverser(stat)

    val declarations = declBuilder.result()
    val references = refBuilder.result()

    val inliningCandidates = references
      .groupBy(identity)
      .filter(_._2.size == 1)
      .keySet
      .intersect(declarations)

    @tailrec
    def recurse(remStats: List[Stat], alreadyProcessed: List[Stat]): List[Stat] = {
      remStats match {
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, lhs) :: (methodDef: Defn.Def) :: (appl@Term.Apply(_, args)) :: tail
          if inliningCandidates.contains(nameStr1)
            && args.exists {
            case Term.Name(nameStr2) => nameStr2 == nameStr1
            case _ => false
          }
        => {
          val newArgs = args.map {
            case Term.Name(nameStr2) if nameStr2 == nameStr1 => lhs
            case other => other
          }
          recurse(tail, alreadyProcessed ++ List(methodDef, appl.copy(args = newArgs)))
        }
        case Defn.Val(_, List(Pat.Var(Term.Name(nameStr1))), _, lhs)
          :: (methodDef: Defn.Def)
          :: (finalValDefn@Defn.Val(_, _, _, appl@Term.Apply(_, args)))
          :: tail
          if inliningCandidates.contains(nameStr1)
            && args.exists {
            case Term.Name(nameStr2) => nameStr2 == nameStr1
            case _ => false
          }
        => {
          val newArgs = args.map {
            case Term.Name(nameStr2) if nameStr2 == nameStr1 => lhs
            case other => other
          }
          recurse(tail, alreadyProcessed ++ List(methodDef, finalValDefn.copy(rhs = appl.copy(args = newArgs))))
        }
        case head :: tail => recurse(tail, alreadyProcessed :+ head)
        case Nil => alreadyProcessed
      }
    }

    val transformed = recurse(stats, Nil)
    if (transformed == stats) stats
    else inlineExternalCalls(transformed)
  }

}
