package carte

import Rules.Rule

case class Violation(rule: Rule, span: Span)
