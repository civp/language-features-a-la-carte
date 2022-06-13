package sbtlanguagefeatures

import syntactic.Checker
import scala.meta.Dialect

import sbt._
import Keys._
import java.io.File

object LanguageFeaturesPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val languageFeaturesConfig = settingKey[LanguageFeaturesConfig]("Configuration of the checker.")
    val languageFeaturesCheck = taskKey[Unit]("Check language features.")
  }

  import autoImport._
  
  // Get files with .scala extension under a directory
  private def getScalaFiles(dir: File): List[File] = {
    val files = dir.listFiles.toList
    files.filter { file =>
      file.getName.split("\\.").lastOption match {
        case Some("scala") => true
        case _ => false
      }
    } ++ files.filter(_.isDirectory).flatMap(getScalaFiles)
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    languageFeaturesCheck := {
      val LanguageFeaturesConfig(dialect, checker) = languageFeaturesConfig.value
      val sourceDir = (Compile / scalaSource).value
      val sourceFiles = getScalaFiles(sourceDir)
      val results = sourceFiles.map(checker.checkFile(dialect, _))
      sourceFiles.zip(results).foreach { case (file, result) =>
        Reporter.report(file, result)
      }
    }
  )

}
