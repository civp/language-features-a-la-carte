package syntactic

import carte.Rules.BlacklistRule

import scala.meta._

/**
 * Specification of language constructs that are forbidden
 * @param checkFunc PartialFunction reporting the forbidden constructs
 * @param msg explanation of why the construct is rejected
 */
sealed abstract class Rule(val checkFunc: PartialFunction[Tree, Rule.Violation], val msg: String)
  extends BlacklistRule

object Rule {

  /**
   * Forbid the use of null
   */
  case object NoNull extends Rule({
    case nullKw: Lit.Null => reportNull(nullKw)
  }, "usage of null is forbidden")
  private def reportNull(kw: Tree) = Violation(kw, NoNull)

  /**
   * Forbid casts with asInstanceOf
   */
  case object NoCast extends Rule({
    case asInstanceOfKw @ Name("asInstanceOf") => reportCast(asInstanceOfKw)
  }, "casts are forbidden")
  private def reportCast(kw: Tree) = Violation(kw, NoCast)

  /**
   * Forbid the use of var
   */
  case object NoVar extends Rule({
    case varKw: Defn.Var => reportVar(varKw)
    case varKw: Decl.Var => reportVar(varKw)
  }, "usage of var is forbidden")
  private def reportVar(kw: Tree) = Violation(kw, NoVar)

  /**
   * Forbid the use of imperative loops (while and do-while)
   */
  case object NoWhile extends Rule({
    case whileKw: Term.While => reportImperativeLoop(whileKw)
    case doWhileKw: Term.Do => reportImperativeLoop(doWhileKw)
  }, "usage of while and do-while loops is forbidden")
  private def reportImperativeLoop(kw: Tree) = Violation(kw, NoWhile)

  /**
   * Violation of a rule
   * @param forbiddenNode node that violates the rule
   * @param violatedRule rule that rejected this node
   */
  case class Violation(val forbiddenNode: Tree, val violatedRule: Rule){
    val pos: Position = forbiddenNode.pos

    override def toString: String = s"${pos.startLine}:${pos.startColumn}: ${violatedRule.msg}"
  }

}
