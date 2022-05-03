package output.example

import syntactic.blacklist.BlacklistChecker

case class Output(checker: BlacklistChecker, expected: List[(Int, Int)])
