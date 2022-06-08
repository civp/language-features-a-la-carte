import Dependencies._

lazy val scala2Version = "2.13.8"
lazy val scala3Version = "3.1.2"

lazy val tastyQueryJVM = project
  .in(file("tasty-query"))

lazy val semantic = project
  .in(file("semantic"))
  .settings(
    scalaVersion := scala3Version
  )
  .dependsOn(tastyQueryJVM)

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

lazy val testkit = project
  .in(file("testkit"))
  .settings(
    scalaVersion := scala2Version,
    libraryDependencies += scalameta
  )

lazy val testsInput = project
  .in(file("tests/input"))
  .settings(
    scalaVersion := scala3Version
  )

lazy val testsUnit = project
  .in(file("tests/unit"))
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies += munit,
    fork := true,
    javaOptions ++= {
      val testsInputSource = (testsInput / Compile / scalaSource).value
      val testsInputProduct = {
        val testsInputProducts = (testsInput / Compile / products).value
        // Only one output location expected
        assert(testsInputProducts.size == 1)
        testsInputProducts.map(_.getAbsolutePath).head
      }
      Seq(
        s"-Dtests-input-source=$testsInputSource",
        s"-Dtests-input-product=$testsInputProduct"
      )
    }
  )
  .dependsOn(syntactic, semantic, testkit)
