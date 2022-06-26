package features.sbtplugin

import features.syntactic.Checker
import features.syntactic.blacklist.BlacklistChecker
import features.syntactic.whitelist.{WhitelistChecker, FeaturesProvider, PredefFeatures}

import scala.meta.Dialect

private[sbtplugin] abstract class LanguageFeaturesConfig protected (
    val dialect: Dialect,
    val checker: Checker
)

private[sbtplugin] final case class BlacklistFeaturesConfig (
    override val dialect: Dialect,
    override val checker: BlacklistChecker
) extends LanguageFeaturesConfig(dialect, checker)

private[sbtplugin] final case class WhitelistFeaturesConfig private (
    override val dialect: Dialect,
    override val checker: WhitelistChecker,
    featuresProvider: FeaturesProvider
) extends LanguageFeaturesConfig(dialect, checker)

object LanguageFeaturesConfig {

  def apply(
      dialect: Dialect,
      checker: BlacklistChecker
  ): BlacklistFeaturesConfig =
    BlacklistFeaturesConfig(dialect, checker)

  def apply(
      dialect: Dialect,
      checker: WhitelistChecker,
      featuresProvider: FeaturesProvider = PredefFeatures
  ): WhitelistFeaturesConfig =
    WhitelistFeaturesConfig(dialect, checker, featuresProvider)

  def unapply(config: LanguageFeaturesConfig): Option[(Dialect, Checker)] =
    Some((config.dialect, config.checker))
}
