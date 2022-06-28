lazy val root = project
  .in(file("."))
  .settings(
    version := "0.1",
    scalaVersion := "3.1.2",
    languageFeaturesConfig := LanguageFeaturesConfig(
      Scala3,
      WhitelistChecker(
        LiteralsAndExpressions,
        Defs,
        BasicOop,
        PolymorphicTypes
      ),
      CustomizedFeaturesProvider
    )
  )