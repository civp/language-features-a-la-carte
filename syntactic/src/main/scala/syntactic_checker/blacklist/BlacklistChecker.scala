package syntactic_checker.blacklist

import syntactic_checker.{Checker, Violation}

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

  override def checkTree(tree: Tree): Option[Violation] = {
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
  case class BlacklistViolation(override val forbiddenNode: Tree, val violatedRule: BlacklistRule) extends Violation {

    override def toString: String = s"${pos.startLine}:${pos.startColumn}: ${violatedRule.msg}"
  }

}
