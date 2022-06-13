import sbtlanguagefeatures.LanguageFeaturesConfig
import sbtlanguagefeatures.interfaces._

lazy val root = project
  .in(file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.13.8",
    languageFeaturesConfig := LanguageFeaturesConfig(
      Scala213,
      WhitelistChecker(
        LiteralsAndExpressions,
        Nulls,
        Vals,
        Defs,
        LiteralFunctions,
        ForExpr,
        PolymorphicTypes,
        Laziness,
        ImperativeConstructs,
        ContextualConstructs,
        Extensions,
        Metaprogramming,
        Packages,
        Imports,
        Exports,
        Xml,
        StringInterpolation,
        Annotations,
        Infixes,
        Inlines
      )
    )
  )