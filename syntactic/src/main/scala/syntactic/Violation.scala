package syntactic

import scala.meta.{Position, Tree}

/**
 * @param forbiddenNode the AST node that is present but not allowed
 * @param msg an optional explanation of the violation
 */
case class Violation(forbiddenNode: Tree, msg: Option[String] = None) {

  def pos: Position = forbiddenNode.pos

  def startLine: Int = pos.startLine

  def endLine: Int = pos.endLine

  def startColumn: Int = pos.startColumn

  def endColumn: Int = pos.endColumn

}


object Violation {
  def apply(forbiddenNode: Tree, msg: String): Violation = Violation(forbiddenNode, Some(msg))
}