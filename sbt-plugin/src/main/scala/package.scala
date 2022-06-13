package sbtlanguagefeatures

package object interfaces {

  import scala.meta.dialects
  val Scala3 = dialects.Scala3
  val Scala213 = dialects.Scala213
  val Scala212 = dialects.Scala212

  import syntactic.blacklist
  val BlacklistChecker = blacklist.BlacklistChecker

  import syntactic.blacklist.BlacklistRules
  val NoCast = BlacklistRules.NoCast
  val NoNull = BlacklistRules.NoNull
  val NoVar = BlacklistRules.NoVar
  val NoWhile = BlacklistRules.NoWhile

  import syntactic.whitelist
  val WhitelistChecker = whitelist.WhitelistChecker

  import syntactic.whitelist.PredefFeatures
}