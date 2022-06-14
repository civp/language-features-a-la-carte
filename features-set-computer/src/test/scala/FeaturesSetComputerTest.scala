import FeaturesSetComputerTest.TestFeaturesProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import syntactic.CheckResult
import syntactic.whitelist.Feature.{AtomicFeature, CompositeFeature}
import syntactic.whitelist.{FeaturesProvider, PredefFeatures, WhitelistChecker}

import scala.meta.{Defn, Source, Term, Tree, XtensionParseInputLike}

class FeaturesSetComputerTest {

  private def parse(codeStr: String): Source = codeStr.parse[Source].get

  @Test
  def featuresSetComputationFromTreeTest(): Unit = {
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
  def featuresSetComputationFromViolationsTest(): Unit = {
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
    val checkResult = initChecker.checkTree(src)
    assert(checkResult.isInstanceOf[CheckResult.Invalid])
    val featuresSetComputer = new FeaturesSetComputer(PredefFeatures.allDefinedFeatures)
    val requiredFeatures = featuresSetComputer.minimalFeaturesSetToResolve(checkResult.asInstanceOf[CheckResult.Invalid].violations)
    val exp = Some(Set(PredefFeatures.Vals, PredefFeatures.ImperativeConstructs, PredefFeatures.Defs))
    assertEquals(exp, requiredFeatures)
  }

  @Test
  def nonDisjointFeaturesSetTest(): Unit = {
    val codeStr =
      """
        |object Bar {
        |  def baz(s: String, u: Int): Boolean = {
        |    val cst = 42
        |    var p = u + cst
        |    for (ch <- s.toList){
        |      p += u
        |    }
        |    u % 3 == 0
        |  }
        |}
        |""".stripMargin
    val src = parse(codeStr)
    val availableFeatures = TestFeaturesProvider.allDefinedFeatures ++ List(PredefFeatures.LiteralsAndExpressions, PredefFeatures.ForExpr)
    val featuresSetComputer = new FeaturesSetComputer(availableFeatures)
    val exp = Some(Set(
      TestFeaturesProvider.VarsAndValsFt,
      TestFeaturesProvider.DefAndObjectFt,
      PredefFeatures.LiteralsAndExpressions,
      PredefFeatures.ForExpr
    ))
    assertEquals(exp, featuresSetComputer.minimalFeaturesSetFor(src))
  }

}

object FeaturesSetComputerTest {

  private object TestFeaturesProvider extends FeaturesProvider {

    case object VarsFt extends AtomicFeature {
      override val checkPF: PartialFunction[Tree, Boolean] = {
        case _ : Defn.Var => true
      }
    }

    case object VarsAndValsFt extends AtomicFeature {
      override val checkPF: PartialFunction[Tree, Boolean] = {
        case _ : Defn.Var => true
        case _ : Defn.Val => true
      }
    }

    case object OopFt extends AtomicFeature {
      override val checkPF: PartialFunction[Tree, Boolean] = {
        case _ : Defn.Object => true
        case _ : Defn.Class => true
        case _ : Defn.Trait => true
      }
    }

    case object DefFt extends AtomicFeature {
      override val checkPF: PartialFunction[Tree, Boolean] = {
        case _ : Defn.Def => true
        case _ : Term.Param => true
      }
    }

    case object DefAndObjectFt extends CompositeFeature(DefFt, OopFt)

    case object ImcompleteForFt extends AtomicFeature {
      override val checkPF: PartialFunction[Tree, Boolean] = {
        case _ : Term.For => true
      }
    }

  }

}
