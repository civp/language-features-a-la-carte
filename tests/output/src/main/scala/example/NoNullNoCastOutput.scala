package output.example

import syntactic_checker.blacklist.{BlacklistChecker, BlacklistRule}

object NoNullNoCastOutput extends Output (

  BlacklistChecker(BlacklistRule.NoNull, BlacklistRule.NoCast),
  List(
    (11, 48),
    (12, 51),
    (22, 18),
    (23, 20)
  )
)
