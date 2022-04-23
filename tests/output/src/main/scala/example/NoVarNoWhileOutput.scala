package output.example

import syntactic_checker.blacklist.{BlacklistChecker, BlacklistRule}

object NoVarNoWhileOutput extends Output (

  BlacklistChecker(BlacklistRule.NoVar, BlacklistRule.NoWhile),
  List(
    (2, 2),
    (3, 2),
    (16, 6),
    (17, 6),
    (20, 8),
    (26, 6)
  )
)
