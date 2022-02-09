import Rule._
import scala.meta._

class Checker private(rules: List[Rule]) {
  require(rules.nonEmpty, "checker must have at least 1 rule")

  private val defaultRule: PartialFunction[Tree, Option[Violation]] = {
    case _ => None
  }

  private val combinedCheckFunc = {
    val checkFuncs = rules.map(_.checkFunc.andThen(Some(_)))
    checkFuncs.tail
      .foldLeft[PartialFunction[Tree, Option[Violation]]](checkFuncs.head)(_.orElse(_))
      .orElse(defaultRule)
  }

  def check(source: Source): List[Violation] = {
    source.collect(combinedCheckFunc).flatten
  }

}

object Checker {

  def apply(rule: Rule, rules: Rule*): Checker = new Checker(rule :: rules.toList)
  def apply(rules: List[Rule]): Checker = new Checker(rules)

}
