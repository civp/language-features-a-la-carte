package semantic

import tastyquery.ast.Types.Type
import tastyquery.ast.Names.TermName
import tastyquery.ast.Spans.Span

case class Method(tpe: Type, name: TermName, span: Span)
