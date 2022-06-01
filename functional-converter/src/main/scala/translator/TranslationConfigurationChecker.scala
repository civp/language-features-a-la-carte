package translator

import syntactic.CheckResult
import syntactic.whitelist.{WhitelistChecker, Features => F}

import java.util.StringJoiner
import scala.meta.{Decl, Defn, Name, Pat}

class TranslationConfigurationChecker(reporter: Reporter) {
  require(reporter != null)

  private val checker = WhitelistChecker(
    F.Vals, F.Defs, F.Nulls, F.LiteralsAndExpressions,
    F.Annotations, F.ForExpr, F.ImperativeConstructs, F.Laziness,
    F.PolymorphicTypes, F.StringInterpolation
  )

  def checkCanConvert(method: Defn.Def): Boolean =
    checkThatUsedConstructsAllowConversion(method) && checkThatValAndVarNamesAllowConversion(method)

  private def checkThatUsedConstructsAllowConversion(method: Defn.Def): Boolean = {
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

  private def checkThatValAndVarNamesAllowConversion(method: Defn.Def): Boolean = {

    def findDeclarations(pats: List[Pat]): List[String] = pats.flatMap(_.collect { case Name(nameStr) => nameStr })

    val namesDefinedInValOrVarDefOrDecl = method.collect {
      case v: Defn.Val => findDeclarations(v.pats)
      case v: Defn.Var => findDeclarations(v.pats)
      case v: Decl.Val => findDeclarations(v.pats)
      case v: Decl.Var => findDeclarations(v.pats)
    }.flatten

    val duplicated = namesDefinedInValOrVarDefOrDecl.groupBy(identity).values.filter(_.size > 1).map(_.head).toList
    val msgJoiner = new StringJoiner("\n")
    if (duplicated.nonEmpty) {
      msgJoiner.add(s"Cannot convert method ${method.name.value}:")
    }
    duplicated.foreach { name =>
      msgJoiner.add(s"variable shadowing detected on identifier $name")
    }
    if (duplicated.nonEmpty) {
      reporter.addErrorMsg(msgJoiner.toString)
    }
    duplicated.isEmpty
  }

}
