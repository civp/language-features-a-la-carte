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
          TermRef(PackageRef(SimpleName("scala")), SimpleName("package")),
          TypeName(SimpleName("List"))),
        _),
      SimpleName("head"),
      _) => Violation(m, Some("List#head is unsafe"))
  }) 

  case object NoOptionGet extends Rule({
    case m => Violation(m)
  })

  case object NoPrintln extends Rule({
    case m => Violation(m)
  })

  case object NoIterableOnceOpsForeach extends Rule({
    case m => Violation(m)
  })

}
