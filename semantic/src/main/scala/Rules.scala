package semantic

import tastyquery.ast.Trees.Tree
import tastyquery.ast.Types._
import tastyquery.ast.Symbols._
import tastyquery.ast.Names._

sealed abstract class Rule(val checkFunc: PartialFunction[Method, Violation])

object Rules {

  case object NoListHead extends Rule({
    case m @ Method(
      AppliedType(
        TypeRef(
          _,
          TypeName(SimpleName("List"))),
        _),
      SimpleName("head"),
      _) => Violation(m, "List#head is unsafe")
  }) 

  case object NoOptionGet extends Rule({
    case m @ Method(
      AppliedType(
        TypeRef(
          _,
          TypeName(SimpleName("Option"))),
        _),
      SimpleName("get"),
      _) => Violation(m, "Option#get is unsafe")
  })

  case object NoPrintln extends Rule({
    case m @ Method(
      NoType,
      SimpleName("println"),
      _
    ) => Violation(m, "println performs side-effects")
  })

  case object NoIterableOnceOpsForeach extends Rule({
    case m @ Method(
      _, // TODO: is subtype
      SimpleName("foreach"),
      _
    ) => Violation(m, "foreach performs side-effects")
  })

}