package features.translator

import scala.meta.{Term, Tree}

/**
 * Translator assumes that all assignations are performed using the `=` operator. Operators like
 * `+=`, `%=`, etc. must therefore be converted to uses of `=`
 */
object AssignationsHandler {

  /**
   * Convert `x += 1` to `x = x + 1` (and similar)
   */
  def makeAssignationsExplicit(tree: Tree): Tree = {
    tree.transform {
      case Term.ApplyInfix(lhs, Term.Name(op), targs, args)
        if op.length >= 2 && op.last == '=' && assignableOperators.contains(op.dropRight(1))
      =>
        Term.Assign(lhs, Term.ApplyInfix(lhs, Term.Name(op.init), targs, args))
      case anyTree => anyTree
    }
  }

  /**
   * Convert `x = x + 1` to `x += 1` (and similar)
   */
  def makeAssignationsCompact(tree: Tree): Tree = {
    tree.transform {
      case Term.Assign(lhs@Term.Name(lhs1), Term.ApplyInfix(Term.Name(lhs2), Term.Name(op), targs, args))
        if lhs1 == lhs2 && assignableOperators.contains(op)
      =>
        Term.ApplyInfix(lhs, Term.Name(op + "="), targs, args)
      case anyTree => anyTree
    }
  }

  // This list comes from https://www.tutorialspoint.com/scala/scala_operators.htm
  private val assignableOperators = List("+", "-", "*", "/", "%", "<<", ">>", "&", "^", "|")

}
