package carte

import Rules.{Rule, NoRule}
import Spans.{Span, NoSpan}

object Violations {

  abstract class Violation(rule: Rule, span: Span) {
    def toString: String
  }

  object NoViolation extends Violation(NoRule, NoSpan) {
    override def toString: String = ""
  }
}