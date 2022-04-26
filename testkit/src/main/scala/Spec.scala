package testkit

import carte.Rules.{Rule, NoRule}
import carte.Violations.{Violation, NoViolation}
import carte.Spans.{Span, NoSpan}
import syntactic.{Rule => SyntacticRules}
import syntactic.Rule.{Violation => _, _}

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator._
import scala.util.parsing.input.Position

/**
  * A wrapper for violation
  * Assuming the spans of violations never overlap each other
  *
  * @param violation
  */
case class Spec(violation: Violation)

object EmptySpec extends Spec(NoViolation)

object Spec extends StandardTokenParsers {

  lexical.delimiters ++= List(":", "^")
  lexical.reserved ++= List("violation", "NoNull", "NoCast", "NoVar", "NoWhile")

  def rule: Parser[Rule] =
    // TODO: generalize to more rules
    "NoNull" ^^^ NoNull |
    "NoCast" ^^^ NoCast |
    "NoVar" ^^^ NoVar |
    "NoWhile" ^^^ NoWhile
  def span: Parser[Span] =
    // TODO: implement offset
    rep("^") ^^^ NoSpan
  // TODO: support multiple violations on one line
  def spec: Parser[Spec] = "violation" ~ ":" ~ rule ~ span ^^ {
    case _ ~ _ ~ r ~ sp => new Spec(Violation(r, sp))
  }

  def fromString(str: String): Spec = {
    val tokens = new lexical.Scanner(str)
    phrase(spec)(tokens) match {
      case Success(spc, in) => { println(in.pos); spc }
      case e => {
        println(e)
        EmptySpec
      }
    }
  }
}