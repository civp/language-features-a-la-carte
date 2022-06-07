import Dependencies._

lazy val scala2Version = "2.13.8"
lazy val scala3Version = "3.1.2"

lazy val tastyQueryJVM = project
  .in(file("tasty-query"))

lazy val semanticTestResouces = project
  .in(file("semantic/src/test/res"))
  .settings(scalaVersion := scala3Version)

lazy val semantic = project
  .in(file("semantic"))
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies += munit,
    fork := true,
    javaOptions += {
      val testResources = {
        val testSourcesProducts = (semanticTestResouces / Compile / products).value
        // Only one output location expected
        assert(testSourcesProducts.size == 1)
        testSourcesProducts.map(_.getAbsolutePath).head
      }
      s"-Dtest-resources=$testResources"
    }
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
  .dependsOn(syntactic, semantic)

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
