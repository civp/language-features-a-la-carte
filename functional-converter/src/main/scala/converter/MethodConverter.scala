package converter

import syntactic.CheckResult
import syntactic.whitelist.{WhitelistChecker, Features => F}

import java.util.StringJoiner
import scala.meta.Term.Block
import scala.meta.{Decl, Defn, Name, Pat, Stat, Term, Tree}
import scala.util.Try

class MethodConverter(reporter: Reporter) {
  private val checker = WhitelistChecker(
    F.Vals, F.Defs, F.Nulls, F.LiteralsAndExpressions,
    F.Annotations, F.ForExpr, F.ImperativeConstructs, F.Laziness,
    F.PolymorphicTypes, F.StringInterpolation
  )

  def checkThatUsedConstructsAllowConversion(method: Defn.Def): Boolean = {
    checker.checkTree(method) match {
      case CheckResult.Valid => true
      case CheckResult.Invalid(violations) =>
        reporter.addErrorMsg(
          s"Cannot convert method ${method.name.value} because of the following constructs: ${violations}"
        )
        false
      case CheckResult.ParsingError(cause) =>
        reporter.addErrorMsg(s"Cannot convert method ${method.name.value} because of a syntax error: ${cause.getMessage}")
        false
    }
  }

  def checkThatValAndVarNamesAllowConversion(method: Defn.Def): Boolean = {

    def findDeclarations(pats: List[Pat]): List[String] = pats.flatMap(_.collect { case Name(nameStr) => nameStr })

    val namesDefinedInValOrVarDefOrDecl = method.collect {
      case v: Defn.Val => findDeclarations(v.pats)
      case v: Defn.Var => findDeclarations(v.pats)
      case v: Decl.Val => findDeclarations(v.pats)
      case v: Decl.Var => findDeclarations(v.pats)
    }.flatten

    val duplicated = namesDefinedInValOrVarDefOrDecl.groupBy(identity).values.filter(_.size > 1).map(_.head).toList
    val msgJoiner = new StringJoiner("\n")
    if (duplicated.nonEmpty){
      msgJoiner.add(s"Cannot convert ${method.name.value}:")
    }
    duplicated.foreach { name =>
      msgJoiner.add(s"variable shadowing detected on identifier $name")
    }
    if (duplicated.nonEmpty){
      reporter.addErrorMsg(msgJoiner.toString)
    }
    duplicated.isEmpty
  }

  def convert(method: Defn.Def): Defn.Def = {
    val canConvert = checkThatUsedConstructsAllowConversion(method) && checkThatValAndVarNamesAllowConversion(method)
    if (canConvert){

    }
    else method
  }

  private case class PartialConversionResult(stats: List[Stat], context: IdentifiersContext)

  private def convertBlockOrSingleStat(tree: Tree, initCtx: IdentifiersContext): Tree = {
    tree match {
      case Block(stats) =>
        val partRes = convertStatsSeq(stats, initCtx)
        Block(partRes.stats)
      case uniqueStat =>
    }
  }

  private def convertStatsSeq(stats: List[Stat], initCtx: IdentifiersContext): PartialConversionResult = {

  }

}
