# Language Features a la carte

A tool checking that some specific language features are not used

## Syntactic checkers
Defining which constructs are allowed to be used in a program can be done in two ways:
* Blacklist-based checking: specify the constructs that <b>should not</b> be used
* Whitelist-based checking: specify the constructs that <b>are allowed</b> to be used (the other constructs are forbidden)

### Blacklisting language features
To check a program using a blacklist-based approach, instantiate a `BlacklistChecker` with
`BlacklistRule`s that match the constructs that should not be allowed.
`BlacklistRule`s can be either predefined ones, available in `PredefBlacklistRules` (e.g.
`PredefBlacklistRules.NoVar`) or custom ones.

E.g. to check that a program does not use `while` or `var`:
```Scala
import syntactic.blacklist.{ BlacklistChecker, PredefBlacklistRules }
import scala.meta.dialects.Scala3

val sourceCodeString = """
                         |private val y: String = f(x)
                         |""".stripMargin

val checker = BlacklistChecker(
  PredefBlacklistRules.NoVar,
  PredefBlacklistRules.NoWhile
)

checker.checkCodeString(dialect = Scala3, sourceCodeString) // returns Valid
```

To define a custom `BlacklistRule`, create an
object named following the name of your rule and let it extend `BlacklistRule` with as `checkFunc`
a `PartialFunction` that returns a list of `BlackListViolation`s when given a construct that should be blacklisted (for
other constructs it should not be defined, or return an empty list).

E.g. defining a rule that forbids the usage of `null` can be done as follows:
```Scala
case object NoNull extends BlacklistRule {
    override val checkFunc: PartialFunction[Tree, List[Violation]] = {
      case nullKw: Lit.Null =>
        Violation(nullKw, "usage of null is forbidden").toSingletonList
    }
}
```
(the `NoNull` rule of this example is actually implemented in `PredefBlackListRules`, so in practice it should not
be redefined)

`toSingletonList` is a convenience method that is useful because most of the time only one `Violation` has to be returned.

### Whitelisting language features
To check a program using a whitelist-based approach, instantiate a `WhitelistChecker` with all the
`Feature`s that the program under check should be allowed to contain. `Feature`s can be considered as
"whitelist rules", they match a set of language constructs that are therefore allowed in programs.
Similarly to `BlacklistRule`s, `Feature`s can be either predefined (predefined `Feature`s are available
in `PredefFeatures`, e.g. `PredefFeatures.ForExpr`) or user-defined.

E.g.:
```Scala
import syntactic.whitelist.{PredefFeatures, WhitelistChecker}
import scala.meta.dialects.Scala3

// example source code
val sourceCodeString = """
                         |val y: String = f(x)
                         |""".stripMargin

val checker = WhitelistChecker(
  PredefFeatures.LiteralsAndExpressions,
  PredefFeatures.Vals
)

checker.checkCodeString(dialect = Scala3, sourceCodeString)  // return Valid
```

To define a custom `Feature`, create an object that
extends `AtomicFeature` with `checkPF` a `PartialFunction` that returns true when given a construct
that it allows (for other constructs it can either return `false` or not be defined). It is also
possible to define a `Feature` as a `CompositeFeature`, by combining existing `Feature`s.

E.g. defining a `Feature` that allows to define extensions can be done as follows:
```Scala
case object Extensions extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Defn.ExtensionGroup => true
    }
}
```
(the `Extensions` feature of this example is actually implemented in `Features`, so in practice
it should not be redefined)

#### Automatically created list of `Feature`s

The list of all predefined `Feature`s is accessible as `PredefFeatures.allDefinedFeatures`.

It is also possible to automatically create a list of all the `Feature`s that are defined in an object. 
To do that, simply let the object extend `syntactic.whitelist.FeaturesProvider`. The `Features` defined 
in the object can then be found in the `allDefinedFeatures` list. E.g., if `MyCustomFeatures` is defined 
as follows:

```Scala
import syntactic.whitelist.Feature.{AtomicFeature, CompositeFeature}
import syntactic.whitelist.{FeaturesProvider, PredefFeatures}

import scala.meta.{Decl, Defn, Term, Tree}

object MyCustomFeatures extends FeaturesProvider {

  case object ValsAndVars extends CompositeFeature(
    PredefFeatures.Vals,
    PredefFeatures.ImperativeConstructs
  )

  case object MethodsWithoutParam extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Decl.Def => true
      case _: Defn.Def => true
    }
  }

}
```

then `MyCustomFeatures.allDefinedFeatures == List(MethodsWithoutParam, ValsAndVars)`

_Warning_: the creation of the list uses reflection and fails if the object extending 
`FeaturesProvider` is defined on a worksheet (but if it is defined in a regular file 
the list can be referenced from a worksheet).

#### Computing the needed set of `Feature`s

In some cases the exact set of `Feature`s that need to be allowed can be tricky to determine. 
The `FeaturesSetComputer` (in the `features-set-computer` module) can help in these cases. This class should be instantiated with 
a list of all the available `Feature`s as its unique constructor argument, and its 
`minimalFeaturesSetFor` methods map a `Tree` or a list of `Node`s to the minimal set of features 
required for this program to be accepted (wrapped in a `Some`, or `None` if no such set exists).
E.g.:

```Scala
import syntactic.whitelist.PredefFeatures
import scala.meta.{Source, dialects}

val src = dialects.Scala3(
"""
object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello world")
  }
}
""").parse[Source].get

val featuresSetComputer = new FeaturesSetComputer(PredefFeatures.allDefinedFeatures)
featuresSetComputer.minimalFeaturesSetFor(src)  // returns Some(Set(PolymorphicTypes, ADTs, Defs, LiteralsAndExpressions))
```

## Refactoring of loops into tail-recursions

If a program contains loops (`while`, `do-while` or `for`), it is possible (under certain conditions) to automatically 
transform it so that loops are replaced by tail-recursions. To do that, instantiate a `Translator` (`translator.Translator` 
in the `functional-translator` module), and call one of its transformation methods. E.g., the transformation of:

```Scala
def reverseList(ls: List[Int]): List[Int] = {
  var rem = ls
  var result: List[Int] = Nil
  while (rem.nonEmpty){
    val head :: tail = rem
    result = head :: result
    rem = tail
  }
  result
}
```

can be performed by the following code:

```Scala
import translator.{Reporter, Translator}
import scala.meta.{Source, dialects}

val reporter = new Reporter()
val translator = Translator(reporter)

val src = dialects.Scala3(
  """
    |def reverseList(ls: List[Int]): List[Int] = {
    |  var rem = ls
    |  var result: List[Int] = Nil
    |  while (rem.nonEmpty){
    |    val head :: tail = rem
    |    result = head :: result
    |    rem = tail
    |  }
    |  result
    |}
    |""".stripMargin).parse[Source].get

val translated = translator.translateMethodsIn(src)
reporter.getReportedErrors  // returns Nil
```

and `translated` is an AST for the following program:

```Scala
def reverseList(ls: List[Int]): List[Int] = {
  def autoGen_0(result: List[Int], rem: List[Int]): List[Int] = {
    if (rem.nonEmpty) {
      val head :: tail = rem
      autoGen_0(head :: result, tail)
    } else result
  }
  autoGen_0(Nil, ls)
}
```

One may need to add type annotations to some variables in the original program for the translation to work.

## Sbt plugin

The sbt plugin provides an interface for project-wise language features checking.
To use the plugin, first add the plugin to `project/plugins.sbt` and configure the checker
in `buiild.sbt`. E.g.,

```scala
lazy val proj = project
  .in(file("proj"))
  .settings(
    scalaVersion = "3.1.2",
    languageFeaturesConfig := LanguageFeaturesConfig(
      dialect = Scala3,
      checker = WhitelistChecker(
        LiteralsAndExpressions,
        Defs,
        BasicOop,
        AdvancedOop,
        PolymorphicTypes,
      )
    )
```

Then you will be able to run the task `languageFeaturesCheck` in sbt.

To customize language features, define the new language features under the directory `project`.
E.g., to add `UnionTypes` to the predefined features, we need to extend `FeaturesProvider`.
For more details, have a look at [this test case](sbt-plugin/src/sbt-test/sbt-language-features/customized-features).

```scala
object CustomizedFeaturesProvider extends FeaturesProvider {

  case object UnionTypes extends AtomicFeature({
    case _: Type.Or => true
    case _: Type.ApplyInfix => true
  })

}
```

At the moment, semantic checkers are not supported in the sbt plugin because
they depend on TASTy Query which is only available in Scala 2.13/3.
