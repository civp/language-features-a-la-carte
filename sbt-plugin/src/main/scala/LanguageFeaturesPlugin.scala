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

    @inline final val LanguageFeaturesConfig = sbtlanguagefeatures.LanguageFeaturesConfig

    import scala.meta.dialects
    @inline final val Scala3 = dialects.Scala3
    @inline final val Scala213 = dialects.Scala213
    @inline final val Scala212 = dialects.Scala212

    import syntactic.blacklist
    @inline final val BlacklistChecker = blacklist.BlacklistChecker

    import syntactic.blacklist.BlacklistRules
    @inline final val NoCast = BlacklistRules.NoCast
    @inline final val NoNull = BlacklistRules.NoNull
    @inline final val NoVar = BlacklistRules.NoVar
    @inline final val NoWhile = BlacklistRules.NoWhile

    import syntactic.whitelist
    @inline final val WhitelistChecker = whitelist.WhitelistChecker

    import syntactic.whitelist.PredefFeatures
    @inline final val LiteralsAndExpressions = PredefFeatures.LiteralsAndExpressions
    @inline final val Nulls = PredefFeatures.Nulls
    @inline final val Vals = PredefFeatures.Vals
    @inline final val Defs = PredefFeatures.Defs
    @inline final val ADTs = PredefFeatures.ADTs
    @inline final val LiteralFunctions = PredefFeatures.LiteralFunctions
    @inline final val ForExpr = PredefFeatures.ForExpr
    @inline final val PolymorphicTypes = PredefFeatures.PolymorphicTypes
    @inline final val Laziness = PredefFeatures.Laziness
    @inline final val BasicOop = PredefFeatures.BasicOop
    @inline final val AdvancedOop = PredefFeatures.AdvancedOop
    @inline final val ImperativeConstructs = PredefFeatures.ImperativeConstructs
    @inline final val ContextualConstructs = PredefFeatures.ContextualConstructs
    @inline final val Extensions = PredefFeatures.Extensions
    @inline final val Metaprogramming = PredefFeatures.Metaprogramming
    @inline final val Packages = PredefFeatures.Packages
    @inline final val Imports = PredefFeatures.Imports
    @inline final val Exports = PredefFeatures.Exports
    @inline final val Xml = PredefFeatures.Xml
    @inline final val StringInterpolation = PredefFeatures.StringInterpolation
    @inline final val Annotations = PredefFeatures.Annotations
    @inline final val Infixes = PredefFeatures.Infixes
    @inline final val Inlines = PredefFeatures.Inlines
  }

  import autoImport._
  
  // Get files with .scala extension under the directory
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
  
  /**
    * Update messages of whitelist violations with missing features
    *
    * @param result CheckResult with default messages
    * @return The updated CheckResult (with updated messages)
    */
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
