package semantic

import tastyquery.ast.Trees.Tree

sealed abstract class Rule(val checkFunc: PartialFunction[Tree, Violation], val msg: String)

// object Rule {
// 
//   case object SafetyRule extends Rule({
// 
//   }, s"usage of $method is forbidden")
// 
//   case object FunctionalRule extends Rule({
// 
//   }, s"method $method is forbidden")
// }
