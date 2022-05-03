package output.example

import syntactic.blacklist.BlacklistRules

import syntactic.blacklist.{BlacklistChecker, BlacklistRule}

object NoNullNoCastOutput extends Output (

  BlacklistChecker(BlacklistRules.NoNull, BlacklistRules.NoCast),
  List(
    (11, 48),
    (12, 51),
    (22, 18),
    (23, 20)
  )
)
