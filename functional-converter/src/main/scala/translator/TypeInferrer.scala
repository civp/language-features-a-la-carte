package translator

import scala.meta.{Lit, Term, Type}

/**
 * Utility module that performs basic type inference
 */
object TypeInferrer {

  /**
   * @return the type, wrapped in a some, if it can infer it, otherwise None
   */
  def tryToInferType(expr: Term, namingContext: NamingContext): Option[Type] = {
    expr match {
      case Term.Name(nameStr) if namingContext.currentlyReferencedVals.contains(nameStr) =>
        namingContext.currentlyReferencedVals(nameStr)
      case Term.Name(nameStr) if namingContext.currentlyReferencedVars.contains(nameStr) =>
        namingContext.currentlyReferencedVars.get(nameStr).map(_.typ)
      case _: Lit.Int => someType("Int")
      case _: Lit.Boolean => someType("Boolean")
      case _: Lit.String => someType("String")
      case _: Lit.Unit => someType("Unit")
      case _: Lit.Double => someType("Double")
      case _: Lit.Float => someType("Float")
      case _: Lit.Char => someType("Char")
      case _: Lit.Long => someType("Long")
      case _: Lit.Short => someType("Short")
      case _: Lit.Byte => someType("Byte")
      case _: Lit.Null => someType("Nothing")
      case _ => None
    }
  }

  private def someType(float: String) = {
    Some(Type.Name(float))
  }
}
