import sbtlanguagefeatures.LanguageFeaturesConfig
import sbtlanguagefeatures.interfaces._

lazy val root = project
  .in(file("."))
  .settings(
    version := "0.1",
    scalaVersion := "2.13.8",
    languageFeaturesConfig := LanguageFeaturesConfig(
      Scala213,
      BlacklistChecker(NoNull, NoVar)
    )
  )