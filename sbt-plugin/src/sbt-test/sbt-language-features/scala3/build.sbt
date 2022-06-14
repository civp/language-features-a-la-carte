import sbtlanguagefeatures.LanguageFeaturesConfig
import sbtlanguagefeatures.interfaces._

lazy val root = project
  .in(file("."))
  .settings(
    version := "0.1",
    scalaVersion := "3.1.2",
    languageFeaturesConfig := LanguageFeaturesConfig(
      Scala3,
      BlacklistChecker(NoNull, NoVar)
    )
  )