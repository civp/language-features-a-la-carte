package syntactic_checker.blacklist

import syntactic_checker.blacklist.BlacklistChecker.BlacklistViolation
import syntactic_checker.{Checker, Violation}

import scala.meta._

/**
 * @param rules rules to be checked
 */
class BlacklistChecker private(rules: List[BlacklistRule]) extends Checker[BlacklistViolation] {
  require(rules.nonEmpty, "checker must have at least 1 rule")

  // matches trees that do not match any rule
  private val defaultPartFunc: PartialFunction[Tree, Option[BlacklistViolation]] = {
    case _ => None
  }

  private val combinedCheckFunc: PartialFunction[Tree, Option[BlacklistViolation]] = {
    val checkFuncs = rules.map(_.checkFunc.andThen(Some(_)))
    checkFuncs
      .reduceLeft(_.orElse(_))
      .orElse(defaultPartFunc)
  }

  override def checkTree(tree: Tree): Option[BlacklistViolation] = {
    combinedCheckFunc.applyOrElse(tree, defaultPartFunc)
  }

}

object BlacklistChecker {

  def apply(rule: BlacklistRule, rules: BlacklistRule*): BlacklistChecker = new BlacklistChecker(rule :: rules.toList)

  /**
   * syntactic_checker.Violation of a rule
   *
   * @param forbiddenNode node that violates the rule
   * @param violatedRule  rule that rejected this node
   */
  case class BlacklistViolation(override val forbiddenNode: Tree, violatedRule: BlacklistRule) extends Violation {

    override def toString: String = s"${pos.startLine}:${pos.startColumn}: ${violatedRule.msg}"
  }

}
