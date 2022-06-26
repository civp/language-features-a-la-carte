package features.syntactic.blacklist

import features.syntactic.Violation

import scala.meta._

/**
 * Specification of language constructs that are forbidden
 *
 * <b>Implementations should only be singleton objects</b>
 *
 */
trait BlacklistRule {

  /**
   * PartialFunction reporting the forbidden constructs
   */
  val checkFunc: PartialFunction[Tree, List[Violation]]
}

object PredefBlacklistRules {

  /**
   * Forbid the use of null
   */
  case object NoNull extends BlacklistRule {
    override val checkFunc: PartialFunction[Tree, List[Violation]] = {
      case nullKw: Lit.Null =>
        Violation(nullKw, "usage of null is forbidden").toSingletonList
    }
  }

  /**
   * Forbid casts with asInstanceOf
   */
  case object NoCast extends BlacklistRule {
    override val checkFunc: PartialFunction[Tree, List[Violation]] = {
      case asInstanceOfKw@Name("asInstanceOf") =>
        Violation(asInstanceOfKw, "casts are forbidden").toSingletonList
    }
  }

  /**
   * Forbid the use of var
   */
  case object NoVar extends BlacklistRule {
    override val checkFunc: PartialFunction[Tree, List[Violation]] = {
      case varKw: Defn.Var => reportVar(varKw)
      case varKw: Decl.Var => reportVar(varKw)
    }
  }

  private def reportVar(kw: Tree): List[Violation] =
    Violation(kw, "usage of var is forbidden").toSingletonList

  /**
   * Forbid the use of while and do-while loops
   */
  case object NoWhile extends BlacklistRule {
    override val checkFunc: PartialFunction[Tree, List[Violation]] = {
      case whileKw: Term.While => reportImperativeLoop(whileKw)
      case doWhileKw: Term.Do => reportImperativeLoop(doWhileKw)
    }
  }

  private def reportImperativeLoop(kw: Tree): List[Violation] =
    Violation(kw, "usage of while and do-while loops is forbidden").toSingletonList

}
