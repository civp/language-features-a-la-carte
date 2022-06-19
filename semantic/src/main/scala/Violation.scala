package semantic

import tastyquery.ast.Spans.{Span, NoSpan}

case class Violation(method: Method, msg: String) {

  def span: Span = method.span

  def hasSpan: Boolean = span.exists

  def start: Int = span.start

  def end: Int = span.end

}
