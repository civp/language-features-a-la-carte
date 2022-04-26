package syntactic_checker.blacklist

import syntactic_checker.blacklist.BlacklistChecker.BlacklistViolation

import scala.meta._

/**
 * Specification of language constructs that are forbidden
 *
 * @param checkFunc PartialFunction reporting the forbidden constructs
 * @param msg       explanation of why the construct is rejected
 */
sealed abstract class BlacklistRule(val checkFunc: PartialFunction[Tree, BlacklistViolation], val msg: String)

object BlacklistRule {

  /**
   * Forbid the use of null
   */
  case object NoNull extends BlacklistRule({
    case nullKw: Lit.Null => reportNull(nullKw)
  }, msg="usage of null is forbidden")

  private def reportNull(kw: Tree) = BlacklistViolation(kw, NoNull)

  /**
   * Forbid casts with asInstanceOf
   */
  case object NoCast extends BlacklistRule({
    case asInstanceOfKw@Name("asInstanceOf") => reportCast(asInstanceOfKw)
  }, msg="casts are forbidden")

  private def reportCast(kw: Tree) = BlacklistViolation(kw, NoCast)

  /**
   * Forbid the use of var
   */
  case object NoVar extends BlacklistRule({
    case varKw: Defn.Var => reportVar(varKw)
    case varKw: Decl.Var => reportVar(varKw)
  }, msg="usage of var is forbidden")

  private def reportVar(kw: Tree) = BlacklistViolation(kw, NoVar)

  /**
   * Forbid the use of imperative loops (while and do-while)
   */
  case object NoWhile extends BlacklistRule({
    case whileKw: Term.While => reportImperativeLoop(whileKw)
    case doWhileKw: Term.Do => reportImperativeLoop(doWhileKw)
  }, msg="usage of while and do-while loops is forbidden")

  private def reportImperativeLoop(kw: Tree) = BlacklistViolation(kw, NoWhile)

}
