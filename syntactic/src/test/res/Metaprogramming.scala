
// Source: https://blog.softwaremill.com/starting-with-scala-3-macros-a-short-tutorial-88e9d2b2584c

def debugSingleImpl(expr: Expr[Any])(using Quotes) =
  '{ println("Value of " + ${Expr(expr.show)} + " is " + $expr) }
