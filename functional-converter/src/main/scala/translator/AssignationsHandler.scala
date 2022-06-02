package translator

import scala.meta.{Term, Tree}

object AssignationsHandler {

  def makeAssignationsExplicit(tree: Tree): Tree = {
    tree.transform {
      case Term.ApplyInfix(lhs, Term.Name(op), targs, args)
        if op.length >= 2 && op.last == '=' && assignableOperators.contains(op.dropRight(1))
      =>
        Term.Assign(lhs, Term.ApplyInfix(lhs, Term.Name(op.init), targs, args))
      case anyTree => anyTree
    }
  }

  def makeAssignationsCompact(tree: Tree): Tree = {
    tree.transform {
      case Term.Assign(lhs@Term.Name(lhs1), Term.ApplyInfix(Term.Name(lhs2), Term.Name(op), targs, args))
        if lhs1 == lhs2 && assignableOperators.contains(op)
      =>
        Term.ApplyInfix(lhs, Term.Name(op + "="), targs, args)
      case anyTree => anyTree
    }
  }

  private val assignableOperators = List("+", "-", "*", "/", "%", "<<", ">>", "^", "&", "|")

}
