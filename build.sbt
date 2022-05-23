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
  .dependsOn(shared)

lazy val shared = project
  .in(file("shared"))

lazy val testkit = project
  .in(file("testkit"))
  .settings(
    libraryDependencies ++= Seq(
      scalameta,
      scalaParserCombinators
    )
  )
  .dependsOn(shared, syntactic, semantic)

lazy val testsInput = project
  .in(file("tests/input"))

lazy val testsUnit = project
  .in(file("tests/unit"))
  .settings(
    libraryDependencies += munit,
    Compile / compile / compileInputs := {
      (Compile / compile / compileInputs)
        .dependsOn(testsInput / Compile / compile)
        .value
    },
    fork := true,
    javaOptions += {
      val testsInputProduct = (testsInput / Compile / scalaSource).value
      s"-Dtests-input=$testsInputProduct"
    }
  )
  .dependsOn(testkit, syntactic)
