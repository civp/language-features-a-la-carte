import Dependencies._
import sbtbuildinfo.BuildInfoPlugin

lazy val semantic = project
  .in(file("semantic"))
  .settings(
    moduleName := "semantic",
  )

lazy val syntactic = project
  .in(file("syntactic"))
  .settings(
    moduleName := "syntactic",
    libraryDependencies ++= Seq(
      scalameta,
      junit,
      junitInterface
    )
  )

lazy val testsInput = project
  .in(file("tests/input"))

lazy val testsOutput = project
  .in(file("tests/output"))
  .dependsOn(syntactic)

lazy val testsUnit = project
  .in(file("tests/unit"))
  .settings(
    buildInfoPackage := "tests",
    buildInfoObject := "BuildInfo",
    libraryDependencies ++= Seq(
      scalatest,
      funsuite,
      scalametaTeskit
    ),
    Compile / compile / compileInputs := {
      (Compile / compile / compileInputs)
        .dependsOn(testsInput / Compile / compile)
        .value
    }
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(syntactic, testsOutput)

