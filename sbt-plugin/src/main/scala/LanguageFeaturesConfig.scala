package sbtlanguagefeatures

import syntactic.Checker
import scala.meta.Dialect

case class LanguageFeaturesConfig(dialect: Dialect, checker: Checker)
