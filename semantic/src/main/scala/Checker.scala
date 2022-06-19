package semantic

import java.nio.file.{Files, Paths}

import tastyquery.ast.Trees._
import tastyquery.ast.TypeTrees._
import tastyquery.ast.Types._
import tastyquery.ast.Names._
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

  private def collectMethods(tree: Tree)(using BaseContext): List[Method] = {
    tree.walkTree({
      // qual.name
      case tr @ Select(qual, name: SimpleName) =>
        val tpe = qual.tpe match {
          case tp: TermRef => tp.underlying
          case tp => tp
        }
        Method(tpe, name, tr.span) :: Nil
      // name(...)
      case tr @ Apply(Ident(dname: DerivedName), _) =>
        Method(NoType, dname.underlying, tr.span) :: Nil
      // qual.name(...)
      case tr @ Apply(TypeApply(SelectIn(qual, name: SignedName, owner), _), _) =>
        val tpe = qual.tpe match {
          case tp: TermRef => tp.underlying
          case tp => tp
        }
        Method(tpe, name.underlying, tr.span) :: Nil
      case _ => Nil
    })(_ ::: _, Nil)
  }

  final def checkTree(tree: Tree)(using BaseContext): List[Violation] = {
    collectMethods(tree).flatMap(checkMethod)
  }

  final def checkClass(className: String)(using BaseContext): List[Violation] = {
    val query = reader.read(className)
    // TODO: change the interface
    val tree = query.trees.trees.head
    checkTree(tree)
  }

}

object Checker {

  def apply(rule: Rule, rules: Rule*): Checker = new Checker(rule :: rules.toList)

}