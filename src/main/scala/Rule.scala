
import scala.meta._

sealed abstract class Rule(val checkFunc: PartialFunction[Tree, Rule.Violation], val msg: String)

object Rule {

  case object NoNull extends Rule({
    case nullKw: Lit.Null => reportNull(nullKw)
  }, "usage of null is forbidden")
  private def reportNull(kw: Tree) = Violation(kw, NoNull)

  case object NoCast extends Rule({
    case asInstanceOfKw @ Name("asInstanceOf") => reportCast(asInstanceOfKw)
  }, "casts are forbidden")
  private def reportCast(kw: Tree) = Violation(kw, NoCast)

  case object NoVar extends Rule({
    case varKw: Defn.Var => reportVar(varKw)
    case varKw: Decl.Var => reportVar(varKw)
  }, "usage of var is forbidden")
  private def reportVar(kw: Tree) = Violation(kw, NoVar)

  case object NoWhile extends Rule({
    case whileKw: Term.While => reportImperativeLoop(whileKw)
    case doWhileKw: Term.Do => reportImperativeLoop(doWhileKw)
  }, "usage of while and do-while loops is forbidden")
  private def reportImperativeLoop(kw: Tree) = Violation(kw, NoWhile)


  case class Violation(val forbiddenNode: Tree, val violatedRule: Rule){
    val pos: Position = forbiddenNode.pos

    override def toString: String = s"${pos.startLine}:${pos.startColumn}: ${violatedRule.msg}"
  }

}
