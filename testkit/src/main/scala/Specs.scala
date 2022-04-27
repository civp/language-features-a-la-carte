package testkit

import carte.Rules.{Rule, NoRule}
import carte.Violations.{Violation, NoViolation}
import carte.Spans.{Span, NoSpan}
import syntactic.{Rule => SyntacticRules}
import syntactic.Rule.{Violation => _, _}

import scala.meta._

object Specs {

  def fromPath(path: TestPath): List[String] = {
    val input = path.read
    val tree = input.parse[Source].get
    val commentTokens = findAllComments(tree.tokens)
    commentTokens.tail.map(tokenToString)
  }

  private def findAllComments(tokens: Tokens): List[Token] = {
    tokens
      .filter { x =>
        x.is[Token.Comment] && x.syntax.startsWith("/*")
      }
      .toList
  }

  private def tokenToString(token: Token): String = {
    val pos = token.pos
    val str = token.syntax.stripPrefix("/*").stripSuffix("*/")
    val lines = str.split("\n")
    val column = lines(1).indexOf('^')
    val msg = lines.last.strip()
    s"${pos.startLine}:${column}: ${msg}"
  }

}
