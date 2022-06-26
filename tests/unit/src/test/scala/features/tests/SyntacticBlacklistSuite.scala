package features.tests

import features.syntactic.blacklist.BlacklistChecker
import features.syntactic.blacklist.PredefBlacklistRules._

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
