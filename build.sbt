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
      scalameta
    )
  )

lazy val testsShared = project
  .in(file("tests/shared"))

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
        .dependsOn(
          testsShared / Compile / compile,
          testsInput / Compile / compile
        )
        .value
    }
  )
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(syntactic, testsOutput)

name := "BachelorProject"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies += "org.scalameta" %% "scalameta" % "4.4.34"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
libraryDependencies += "junit" % "junit" % "4.13.2"

