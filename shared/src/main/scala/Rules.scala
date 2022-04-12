package carte

object Rules {

  // abstract class Rule(feature: Feature) {
  //   def check(path: String): List[Violation]
  // }

  // abstract class BlacklistRule(feature: Feature) extends Rule(feature)

  // abstract class WhitelistRule(feature: Feature) extends Rule(feature)

  // A temporary workaround for blacklist
  abstract class Rule
  abstract class BlacklistRule extends Rule
}
