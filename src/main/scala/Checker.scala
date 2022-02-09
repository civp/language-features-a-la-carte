import Rule._
import scala.meta._

/**
 * @param rules rules to be checked
 */
class Checker(rules: List[Rule]) {
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

}

object Checker {

  def apply(rule: Rule, rules: Rule*): Checker = new Checker(rule :: rules.toList)

}
