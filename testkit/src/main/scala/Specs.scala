package testkit

import scala.meta._
import scala.meta.dialects.Scala3

/**
  * Specifications for test input in the form of comments
  * The comment on the head specifies checker parameters.
  * The other comments mark positions and messages of violations.
  * Comments should be wrapped in /* */
  */
object Specs {

  def load(file: TestFile, dialect: Dialect = Scala3): List[String] = {
    val input = file.read
    val tree = dialect(input).parse[Source].get
    val commentTokens = findAllComments(tree.tokens)
    commentTokens.drop(1).map(tokenToString)
  }

  private def findAllComments(tokens: Tokens): List[Token] = {
    tokens
      .filter { token =>
        token.is[Token.Comment] && token.syntax.startsWith("/*")
      }
      .toList
  }

  private def tokenToString(token: Token): String = {
    val pos = token.pos
    val str = token.syntax.stripPrefix("/*").stripSuffix("*/")
    val lines = str.split("\n")
    val column = lines(1).indexOf('^')
    val msg = lines.last.trim()
    s"${pos.startLine}:${column}: ${msg}"
  }

}
