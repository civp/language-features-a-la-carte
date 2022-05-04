package syntactic.blacklist

import syntactic.Violation

import scala.meta._

/**
 * Specification of language constructs that are forbidden
 *
 * @param checkFunc PartialFunction reporting the forbidden constructs
 */
abstract class BlacklistRule(val checkFunc: PartialFunction[Tree, Violation])

object BlacklistRules {

  /**
   * Forbid the use of null
   */
  case object NoNull extends BlacklistRule({
    case nullKw: Lit.Null => Violation(nullKw, "usage of null is forbidden")
  })

  /**
   * Forbid casts with asInstanceOf
   */
  case object NoCast extends BlacklistRule({
    case asInstanceOfKw@Name("asInstanceOf") => Violation(asInstanceOfKw, "casts are forbidden")
  })

  /**
   * Forbid the use of var
   */
  case object NoVar extends BlacklistRule({
    case varKw: Defn.Var => reportVar(varKw)
    case varKw: Decl.Var => reportVar(varKw)
  })

  private def reportVar(kw: Tree) = Violation(kw, "usage of var is forbidden")

  /**
   * Forbid the use of imperative loops (while and do-while)
   */
  case object NoWhile extends BlacklistRule({
    case whileKw: Term.While => reportImperativeLoop(whileKw)
    case doWhileKw: Term.Do => reportImperativeLoop(doWhileKw)
  })

  private def reportImperativeLoop(kw: Tree) = Violation(kw, "usage of while and do-while loops is forbidden")

}
