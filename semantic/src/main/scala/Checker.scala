package semantic

import java.nio.file.{Files, Paths}

import tastyquery.ast.Trees._
import tastyquery.ast.TypeTrees._
import tastyquery.ast.Names._
import tastyquery.ast.Types.{Type, NoType}
import tastyquery.Contexts.BaseContext
import tastyquery.api.ProjectReader

class Checker private (rules: List[Rule]) {

  private val reader = new ProjectReader

  private type CheckFunction = PartialFunction[Method, Option[Violation]]

  private val defaultCheckFunc: CheckFunction = {
    case _ => None
  }

  private val combinedCheckFunc: CheckFunction = {
    val checkFuncs = rules.map(_.checkFunc.andThen(Some(_)))
    checkFuncs
      .reduceLeft(_.orElse(_))
      .orElse(defaultCheckFunc)
  }

  def checkMethod(method: Method): Option[Violation] = {
    combinedCheckFunc.applyOrElse(method, defaultCheckFunc)
  }

  private def typeOf(tree: Tree)(using BaseContext): Type =
    try {
      tree.tpe
    } catch {
      case e: TypeComputationError => NoType
    }

  private def collectType(tree: Tree)(using BaseContext): Map[Name, Type] = {
    tree
      .walkTree({
        case ValDef(name, tpt, _, _) => (name, tpt.toType) :: Nil
        case _ => Nil
      })(_ ::: _, Nil)
      .foldLeft(Map.empty) {
        case (map, (k, v)) =>
          map + (k -> v)
      }
  }

  private def collectMethod(tree: Tree)(using BaseContext): List[Method] = {
    val nameToType = collectType(tree)
    tree.walkTree({
      case t @ Select(qualifier, name: SimpleName) =>
        val tpe = qualifier match {
          case Ident(nme) => nameToType(nme)
          case _ => qualifier.tpe
        }
        Method(tpe, name, t.span) :: Nil
      case t @ Apply(Ident(dname: DerivedName), _) =>
        Method(NoType, dname.underlying, t.span) :: Nil
      case t @ Apply(Ident(name: SimpleName), _) =>
        Method(NoType, name, t.span) :: Nil
      case _ => Nil
    })(_ ::: _, Nil)
  }

  def checkTree(tree: Tree)(using BaseContext): List[Violation] = {
    collectMethod(tree).flatMap(checkMethod)
  }

  def checkClass(className: String)(using BaseContext): List[Violation] = {
    val query = reader.read(className)
    // TODO: change the interface
    val tree = query.trees.trees.head
    checkTree(tree)
  }

}

object Checker {

  def apply(rule: Rule, rules: Rule*): Checker = new Checker(rule :: rules.toList)

}