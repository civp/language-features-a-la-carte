package testkit

import carte.Rules.Rule

import scala.meta._
import scala.meta.internal.symtab.SymbolTable

final class RuleTest(
  val path: TestPath,
  val run: () => Boolean
)

object RuleTest {

  private def findAllComments(tokens: Tokens): List[String] = {
    tokens
      .filter { x =>
        x.is[Token.Comment] && x.syntax.startsWith("/*")
      }
      .map { x =>
        x.syntax.stripPrefix("/*").stripSuffix("*/")
      }
      .toList
  }

  def fromPath(testPath: TestPath): RuleTest = {
    val run: () => Boolean = { () =>
      val input = testPath.read
      val tree = input.parse[Source].get
      val comments = findAllComments(tree.tokens)
      val config = Config.fromString(comments.head)
      val specs = comments.tail.map(Spec.fromString(_))
      println(specs)
      // TODO: implement check
      // val checker = config.getChecker
      // checker(testPath)
      //
      // val rules = config.toRules()
      // rules.check(testPath)
      true
    }

    new RuleTest(testPath, run)
  }
}
