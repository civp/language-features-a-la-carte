package semantic

import tastyquery.ast.Trees.Tree

/**
 * Same as Violation in syntactic analysis
 * But uses different Trees
 */
case class Violation(val forbiddenNode: Tree, val violatedRule: Rule)//{
// 
//   val pos: Position = forbiddenNode.pos
// 
//   override def toString: String = s"${pos.startLine}:${pos.startColumn}: ${violatedRule.msg}"
// }
