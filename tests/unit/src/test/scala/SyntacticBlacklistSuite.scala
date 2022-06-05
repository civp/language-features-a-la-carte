package tests

import syntactic.blacklist.BlacklistChecker
import syntactic.blacklist.BlacklistRules._

import scala.meta.dialects.Scala213

class SyntacticBlacklistSuite extends SyntacticSuite {
  
  test("no-null-no-cast") {
    val path = getTestPath("blacklist/NoNullCast")
    val checker = BlacklistChecker(NoNull, NoCast)
    checkPath(checker, path, Scala213)
  }

  test("no-var-no-while") {
    val path = getTestPath("blacklist/NoVarWhile")
    val checker = BlacklistChecker(NoVar, NoWhile)
    checkPath(checker, path, Scala213)
  }

}
