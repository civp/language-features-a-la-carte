# Language Features a la carte

A tool checking that some specific language features are not used

## How to use the tool
Defining which constructs are allowed to be used in a program can be done in two ways:
* Blacklist-based checking: specify the constructs that <b>should not</b> be used
* Whitelist-based checking: specify the constructs that <b>are allowed</b> to be used (the other constructs are forbidden)

### Blacklisting language features
To check a program using a blacklist-based approach, instantiate a `BlacklistChecker` with
`BlacklistRule`s that match the constructs that should not be allowed.
`BlacklistRule`s can be either predefined ones, available in `BlacklistRules` (e.g.
`BlacklistRules.NoVar`) or custom ones.

E.g. to check that a program does not use `while` or `var`:
```Scala
import syntactic.blacklist.{ BlacklistChecker, BlacklistRules }
import scala.meta.dialects.Scala3

// example source code
val sourceCodeString = """
  |private val y: String = f(x)
  |""".stripMargin

val checker = BlacklistChecker(
  BlacklistRules.NoVar,
  BlacklistRules.NoWhile
)

checker.checkCodeString(dialect = Scala3, sourceCodeString) // returns Valid
```

To define a custom `BlacklistRule`, create an
object named following the name of your rule and let it extend `BlacklistRule` with as `checkFunc`
a `PartialFunction` that returns a `BlackListViolation` when given a construct that should be blacklisted (for
other constructs it should not be defined).

E.g. defining a rule that forbids the usage of `null` can be done as follows:
```Scala
case object NoNull extends BlacklistRule({
    case nullKw: Lit.Null => Violation(nullKw, "usage of null is forbidden")
})
```
(the `NoNull` rule of this example is actually implemented in `BlackListRules`, so in practice it should not
be redefined)

### Whitelisting language features
To check a program using a whitelist-based approach, instantiate a `WhitelistChecker` with all the
`Feature`s that the program under check should be allowed to contain. `Feature`s can be considered as
"whitelist rules", they match a set of language constructs that are therefore allowed in programs.
Similarly to `BlacklistRule`s, `Feature`s can be either predefined (predefined `Feature`s are available
in `Features`, e.g. `Features.ForExpr`) or user-defined.

E.g.:
```Scala
import syntactic.whitelist.{Features, WhitelistChecker}
import scala.meta.dialects.Scala3

// example source code
val sourceCodeString = """
  |val y: String = f(x)
  |""".stripMargin

val checker = WhitelistChecker(
  Features.LiteralsAndExpressions,
  Features.Vals
)

checker.checkCodeString(dialect = Scala3, sourceCodeString)  // return Valid

```

To define a custom `Feature`, create an object that
extends `AtomicFeature` with `checkPF` a `PartialFunction` that returns true when given a construct
that it allows (for other constructs it can either return `false` or not be defined). It is also
possible to define a `Feature` as a `CompositeFeature`, by combining existing `Feature`s.

E.g. defining a `Feature` that allows to define values using `val` can be done as follows:
```Scala
case object Vals extends AtomicFeature(checkPF = {
    case _: Decl.Val => true
    case _: Defn.Val => true
    case _: Pat.Var => true
    case _: Term.Anonymous => true
})
```
(the `Vals` feature of this example is actually implemented in `Features`, so in practice
it should not be redefined)

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