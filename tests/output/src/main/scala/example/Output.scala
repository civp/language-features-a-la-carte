package output.example

import syntactic_checker.blacklist.BlacklistChecker

case class Output(checker: BlacklistChecker, expected: List[(Int, Int)])
