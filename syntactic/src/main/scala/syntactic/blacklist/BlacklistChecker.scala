package syntactic.blacklist

import syntactic.{Checker, Violation}

import scala.meta._

/**
 * @param rules rules to be checked
 */
class BlacklistChecker private(rules: List[BlacklistRule]) extends Checker {
  require(rules.nonEmpty, "checker must have at least 1 rule")

  // matches trees that do not match any rule
  private val defaultPartFunc: PartialFunction[Tree, Option[Violation]] = {
    case _ => None
  }

  private val combinedCheckFunc: PartialFunction[Tree, Option[Violation]] = {
    val checkFuncs = rules.map(_.checkFunc.andThen(Some(_)))
    checkFuncs
      .reduceLeft(_.orElse(_))
      .orElse(defaultPartFunc)
  }

  override def checkNode(node: Tree): Option[Violation] = {
    combinedCheckFunc.applyOrElse(node, defaultPartFunc)
  }

}

object BlacklistChecker {

  def apply(rule: BlacklistRule, rules: BlacklistRule*): BlacklistChecker = new BlacklistChecker(rule :: rules.toList)

}
