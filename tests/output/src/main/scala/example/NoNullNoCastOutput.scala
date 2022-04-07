package output.example

import syntactic.Checker
import syntactic.Rule

object NoNullNoCastOutput extends Output (

  Checker(Rule.NoNull, Rule.NoCast),
  List(
    (11, 48),
    (12, 51),
    (22, 18),
    (23, 20)
  )
)
