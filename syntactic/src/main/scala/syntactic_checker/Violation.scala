package syntactic_checker

import scala.meta.{Position, Tree}

trait Violation {

  val forbiddenNode: Tree

  final def pos: Position = forbiddenNode.pos

  final def startLine: Int = pos.startLine

  final def endLine: Int = pos.endLine

  final def startColumn: Int = pos.startColumn

  final def endColumn: Int = pos.endColumn

}
