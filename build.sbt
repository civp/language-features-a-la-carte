import Dependencies._
import sbtbuildinfo.BuildInfoPlugin

lazy val scala2Version = "2.13.8"
lazy val scala3Version = "3.1.2"

lazy val semantic = project
  .in(file("semantic"))
  .settings(
    scalaVersion := scala3Version
  )

lazy val syntactic = project
  .in(file("syntactic"))
  .settings(
    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      scalameta,
      junit,
      junitInterface
    )
  )

lazy val shared = project
  .in(file("shared"))
  .settings(
    scalaVersion := scala2Version
  )

lazy val testkit = project
  .in(file("testkit"))
  .settings(
    scalaVersion := scala2Version,
    libraryDependencies ++= Seq(
      scalameta,
      scalaParserCombinators
    )
  )
  .dependsOn(shared, syntactic, semantic)

lazy val testsInput = project
  .in(file("tests/input"))
  .settings(
    scalaVersion := scala2Version,
  )

lazy val testsUnit = project
  .in(file("tests/unit"))
  .settings(
    scalaVersion := scala3Version,
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
  .dependsOn(testkit)
