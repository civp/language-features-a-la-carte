package syntactic

import Rule._
import carte.{Checker => AbstractChecker}

import java.io.File
import scala.meta._
import scala.util.{Try, Using}

/**
 * @param rules rules to be checked
 */
class Checker(rules: List[Rule]) extends AbstractChecker {
  require(rules.nonEmpty, "checker must have at least 1 rule")

  // matches trees that do not match any rule
  private val defaultPartFunc: PartialFunction[Tree, Option[Violation]] = {
    case _ => None
  }

  private val combinedCheckFunc = {
    val checkFuncs = rules.map(_.checkFunc.andThen(Some(_)))
    checkFuncs.tail
      .foldLeft[PartialFunction[Tree, Option[Violation]]](checkFuncs.head)(_.orElse(_))
      .orElse(defaultPartFunc)
  }

  /**
   * Apply the rules to the input program
   * @param source input program
   * @return a list of the violations of the checker rules
   */
  def check(source: Source): List[Violation] = {
    source.collect(combinedCheckFunc).flatten
  }

  /**
   * Parses the input program and applies the rules to it
   * @param sourceCode program to parse and check
   * @return a list of the violations of the checker rules
   */
  def check(sourceCode: String): Try[List[Violation]] = Try {
    val source = sourceCode.parse[Source].get
    check(source)
  }

  /**
   * Loads the program from the file described by the given name and applies the rules to it
   * @param filename name/path of the file to be checked
   * @return a list of the violations of the checker rules
   */
  def checkFile(filename: String): Try[List[Violation]] = {
    val content = Using(scala.io.Source.fromFile(filename)) { bufferedSource =>
      bufferedSource.getLines().mkString("\n")
    }
    content.flatMap(check)
  }

}

object Checker {

  def apply(rule: Rule, rules: Rule*): Checker = new Checker(rule :: rules.toList)

}
