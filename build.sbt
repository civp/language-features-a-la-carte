import Dependencies._

lazy val scala212 = "2.12.15"
lazy val scala213 = "2.13.8"
lazy val scala3 = "3.1.2"
lazy val crossVersions = Seq(scala212, scala213)

lazy val semantic = project
  .in(file("semantic"))

lazy val syntactic = project
  .in(file("syntactic"))
  .settings(
    crossScalaVersions := crossVersions,
    libraryDependencies ++= Seq(
      scalameta,
      junit,
      junitInterface
    )
  )

lazy val featuresSetComputer = project
  .in(file("features-set-computer"))
  .settings(
    moduleName := "features-set-computer",
    crossScalaVersions := crossVersions,
    libraryDependencies ++= Seq(
      scalameta,
      junit,
      junitInterface
    )
  )
  .dependsOn(syntactic)

lazy val functionalConverter = project
  .in(file("functional-converter"))
  .settings(
    moduleName := "functional-converter",
    crossScalaVersions := crossVersions,
    libraryDependencies ++= Seq(
      scalameta,
      junit,
      junitInterface
    )
  )
  .dependsOn(syntactic)

lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-language-features",
    // For scripted tests
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .dependsOn(syntactic, featuresSetComputer)

lazy val testkit = project
  .in(file("testkit"))
  .settings(
    crossScalaVersions := crossVersions,
    libraryDependencies ++= Seq(
      scalameta,
      scalaParserCombinators
    )
  )

lazy val testsInput = project
  .in(file("tests/input"))
  .settings(
    crossScalaVersions := crossVersions,
  )

lazy val testsUnit = project
  .in(file("tests/unit"))
  .settings(
    crossScalaVersions := crossVersions,
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
  .dependsOn(syntactic, testkit)
