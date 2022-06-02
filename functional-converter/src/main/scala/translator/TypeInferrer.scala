package translator

import scala.meta.{Lit, Term, Type}

object TypeInferrer {

  def tryToInferType(expr: Term, namingContext: NamingContext): Option[Type] = {
    expr match {
      case Term.Name(nameStr) if namingContext.currentlyReferencedVals.contains(nameStr) =>
        namingContext.currentlyReferencedVals(nameStr)
      case Term.Name(nameStr) if namingContext.currentlyReferencedVars.contains(nameStr) =>
        namingContext.currentlyReferencedVars.get(nameStr).map(_.typ)
      case _: Lit.Int => Some(Type.Name("Int"))
      case _: Lit.Boolean => Some(Type.Name("Boolean"))
      case _: Lit.String => Some(Type.Name("String"))
      case _: Lit.Unit => Some(Type.Name("Unit"))
      // TODO more cases
      case _ => None
    }
  }

}
