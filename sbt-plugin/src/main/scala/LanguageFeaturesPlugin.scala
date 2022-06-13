package sbtlanguagefeatures

import syntactic.{Checker, CheckResult, Violation}
import syntactic.blacklist.BlacklistChecker
import syntactic.whitelist.WhitelistChecker
import syntactic.whitelist.{Feature, PredefFeatures}
import featuressetcomputer.FeaturesSetComputer

import sbt._
import Keys._
import java.io.File
import scala.meta.Dialect

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

  // TODO: support FeatureProvider
  private val featuresSetComputer = new FeaturesSetComputer(PredefFeatures.allDefinedFeatures)

  private def updated(results: List[CheckResult]): List[CheckResult] =
    results.map(updated)
  
  // Update messages of whitelist violations with missing features
  private def updated(result: CheckResult): CheckResult = {
    result match {
      case CheckResult.Invalid(violations) =>
        val requiredFeatures =
          featuresSetComputer.minimalFeaturesSetToResolve(violations) match {
            case Some(features) => features
            case None => List.empty[Feature]
          }
        val updatedMsg = "missing feature(s): " +
          requiredFeatures.map(_.toString).mkString(", ")
        val updatedViolations = violations.map { case Violation(tree, _) =>
          Violation(tree, updatedMsg)
        }
        CheckResult.Invalid(updatedViolations)
      case _ => result
    }
  }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    languageFeaturesCheck := {
      val LanguageFeaturesConfig(dialect, checker) = languageFeaturesConfig.value
      val sourceDir = (Compile / scalaSource).value
      val sourceFiles = getScalaFiles(sourceDir)
      val results = sourceFiles.map(checker.checkFile(dialect, _))
      val updatedResults = checker match {
        case _: BlacklistChecker => results
        case _: WhitelistChecker => updated(results)
      }
      sourceFiles.zip(updatedResults).foreach { case (file, result) =>
        Reporter.report(file, result)
      }
    }
  )

}
