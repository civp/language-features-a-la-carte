package output.example

import syntactic_checker.blacklist.{BlacklistChecker, BlacklistRules}

object NoVarNoWhileOutput extends Output (

  BlacklistChecker(BlacklistRules.NoVar, BlacklistRules.NoWhile),
  List(
    (2, 2),
    (3, 2),
    (16, 6),
    (17, 6),
    (20, 8),
    (26, 6)
  )
)
