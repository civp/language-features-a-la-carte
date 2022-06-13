package sbtlanguagefeatures

package object interfaces {

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
