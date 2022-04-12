package carte

import Rules.{Rule, NoRule}
import Spans.{Span, NoSpan}

object Violations {

  case class Violation(rule: Rule, span: Span)

  object NoViolation extends Violation(NoRule, NoSpan)
}