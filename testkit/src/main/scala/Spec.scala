package testkit

import carte.Rules.Rule
import carte.Violations.{Violation, NoViolation}
import carte.Spans.{Span, NoSpan}
import syntactic.Rule.{Violation => _, _}

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator._

/**
  * A wrapper for violation
  * Assuming the spans of violations never overlap each other
  *
  * @param violation
  */
case class Spec(violation: Violation)

object EmptySpec extends Spec(NoViolation)

object Spec extends StandardTokenParsers {

  import lexical.StringLit

  lexical.delimiters ++= List(":", "^")
  lexical.reserved ++= List("violation", "NoNull", "NoCast", "NoVar", "NoWhile")

  def rule: Parser[Rule] =
    // TODO: generalize to more rules
    "NoVar" ^^^ NoVar |
    "NoCast" ^^^ NoCast |
    "NoVar" ^^^ NoVar |
    "NoWhile" ^^^ NoWhile
  def span: Parser[Span] =
    "^" ^^^ NoSpan
  def comment: Parser[String] = rep(stringLit) ^^ { _.mkString(" ") }
  // TODO: support multiple violations on one line
  def spec: Parser[Spec] = "violation" ~ ":" ~ rule ~ span ~ comment ^^ {
    case _ ~ _ ~ r ~ sp ~ cmt => new Spec(Violation(r, sp))
  }

  def fromString(str: String): Spec = {
    val tokens = new lexical.Scanner(str)
    phrase(spec)(tokens) match {
      case Success(spc, _) => spc
      case e => {
        println(e)
        EmptySpec
      }
    }
  }
}