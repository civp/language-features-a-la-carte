package output.example

import syntactic.Checker

case class Output(checker: Checker, expected: List[(Int, Int)])
