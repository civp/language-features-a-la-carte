import sbt._

object Dependencies {
  val scalatestVersion = "3.0.8"
  val scalametaVersion = "4.5.1"
  val junitInterfaceVersion = "0.13.3"
  val junitVersion = "4.13.2"
  val funsuiteVersion = "3.2.11"
  val scalaParserCombinatorsVersion = "2.1.1"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  val scalameta = "org.scalameta" %% "scalameta" % scalametaVersion
  val scalametaTeskit = "org.scalameta" %% "testkit" % scalametaVersion % Test
  val junitInterface = "com.github.sbt" % "junit-interface" % junitInterfaceVersion % Test
  val junit = "junit" % "junit" % junitVersion % Test
  val funsuite = "org.scalatest" %% "scalatest-funsuite" % funsuiteVersion  % "test"
  val scalaParserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % scalaParserCombinatorsVersion
}
