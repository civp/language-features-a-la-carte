import org.junit.Assert.assertEquals
import org.junit.Test
import syntactic.{CheckResult, Violation}
import syntactic.whitelist.{PredefFeatures, WhitelistChecker}

import scala.meta.{Defn, Source, XtensionParseInputLike}

class FeaturesSetComputerTest {

  private def parse(codeStr: String): Source = codeStr.parse[Source].get

  @Test
  def featuresSetComputationFromTree(): Unit = {
    val codeStr =
      """
        |object Main {
        |  def main(args: Array[String]): Unit = {
        |    println("Hello world")
        |  }
        |}
        |""".stripMargin
    val src = parse(codeStr)
    val exp = Some(Set(
      PredefFeatures.LiteralsAndExpressions,
      PredefFeatures.Defs,
      PredefFeatures.PolymorphicTypes,
      PredefFeatures.ADTs
    ))
    val featuresSetComputer = new FeaturesSetComputer(PredefFeatures.allDefinedFeatures)
    assertEquals(exp, featuresSetComputer.minimalFeaturesSetFor(src))
  }

  @Test
  def featuresSetComputationFromViolations(): Unit = {
    val codeStr =
      """
        |object Foo {
        |  def bar(z: String): Unit = {
        |    val x = 5 + 42 + z.length
        |    val y = f(x, 0)
        |    println(y)
        |    var u = 1
        |    u += 2
        |    println(u)
        |  }
        |}
        |""".stripMargin
    val src = parse(codeStr)
    val initChecker = WhitelistChecker(PredefFeatures.LiteralsAndExpressions, PredefFeatures.ADTs)
    val checkResult = initChecker.checkSource(src)
    assert(checkResult.isInstanceOf[CheckResult.Invalid])
    val featuresSetComputer = new FeaturesSetComputer(PredefFeatures.allDefinedFeatures)
    val requiredFeatures = featuresSetComputer.minimalFeaturesSetToResolve(checkResult.asInstanceOf[CheckResult.Invalid].violations)
    val exp = Some(Set(PredefFeatures.Vals, PredefFeatures.ImperativeConstructs, PredefFeatures.Defs))
    assertEquals(exp, requiredFeatures)
  }

}
