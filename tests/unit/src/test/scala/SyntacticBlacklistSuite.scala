package tests

import syntactic.blacklist.BlacklistChecker
import syntactic.blacklist.BlacklistRules._

import scala.meta.dialects.Scala213

class SyntacticBlacklistSuite extends SyntacticSuite {
  
  test("no-null-no-cast") {
    val file = getTestFile("blacklist/NoNullCast")
    val checker = BlacklistChecker(NoNull, NoCast)
    checkFile(checker, file, Scala213)
  }

  test("no-var-no-while") {
    val file = getTestFile("blacklist/NoVarWhile")
    val checker = BlacklistChecker(NoVar, NoWhile)
    checkFile(checker, file, Scala213)
  }

}
