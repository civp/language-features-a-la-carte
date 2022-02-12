package output.example

import syntactic.Checker
import syntactic.Rule

object NoVarNoWhileOutput extends Output (

  Checker(Rule.NoVar, Rule.NoWhile),
  List(
    (2, 2),
    (3, 2),
    (16, 6),
    (17, 6),
    (20, 8),
    (26, 6)
  )
)
