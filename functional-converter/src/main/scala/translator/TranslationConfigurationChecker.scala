package translator

import syntactic.CheckResult
import syntactic.whitelist.{WhitelistChecker, Features => F}

import java.util.StringJoiner
import scala.meta.{Decl, Defn, Name, Pat, Tree}

class TranslationConfigurationChecker(reporter: Reporter) {
  require(reporter != null)

  private val checker = WhitelistChecker(
    F.Vals, F.Defs, F.Nulls, F.LiteralsAndExpressions,
    F.Annotations, F.ForExpr, F.ImperativeConstructs, F.Laziness,
    F.PolymorphicTypes, F.StringInterpolation, F.AdvancedOop
  )

  def checkCanConvert(tree: Tree): Boolean =
    checkThatUsedConstructsAllowConversion(tree) && checkThatValAndVarNamesAllowConversion(tree)

  private def checkThatUsedConstructsAllowConversion(tree: Tree): Boolean = {
    val methodName = extractMethodName(tree)
    checker.checkTree(tree) match {
      case CheckResult.Valid => true
      case CheckResult.Invalid(violations) =>
        reporter.addErrorMsg(
          s"Cannot convert method $methodName: ${
            violations.map(violation =>
              s"${violation.forbiddenNode.getClass.getSimpleName} (${limitToNCharacters(violation.forbiddenNode.syntax)}) is forbidden"
            ).mkString("\n", "\n", "")
          }"
        )
        false
      case CheckResult.ParsingError(cause) =>
        reporter.addErrorMsg(s"Cannot convert method $methodName because of a syntax error: ${cause.getMessage}")
        false
    }
  }

  private def checkThatValAndVarNamesAllowConversion(tree: Tree): Boolean = {

    def findDeclarations(pats: List[Pat]): List[String] = pats.flatMap(_.collect { case Name(nameStr) => nameStr })

    val namesDefinedInValOrVarDefOrDecl = tree.collect {
      case v: Defn.Val => findDeclarations(v.pats)
      case v: Defn.Var => findDeclarations(v.pats)
      case v: Decl.Val => findDeclarations(v.pats)
      case v: Decl.Var => findDeclarations(v.pats)
    }.flatten

    val methodName = extractMethodName(tree)
    val duplicated = namesDefinedInValOrVarDefOrDecl.groupBy(identity).values.filter(_.size > 1).map(_.head).toList
    val msgJoiner = new StringJoiner("\n")
    if (duplicated.nonEmpty) {
      msgJoiner.add(s"Cannot convert method $methodName:")
    }
    duplicated.foreach { name =>
      msgJoiner.add(s"variable shadowing detected on identifier $name")
    }
    if (duplicated.nonEmpty) {
      reporter.addErrorMsg(msgJoiner.toString)
    }
    duplicated.isEmpty
  }

  private def extractMethodName(tree: Tree) = {
    tree match {
      case defnDef: Defn.Def => s"${defnDef.name.value}"
      case _ => "<unknown>"
    }
  }

  private def limitToNCharacters(str: String, n: Int = 50): String = {
    require(n >= 4)
    if (str.length <= n) str
    else s"${str.take(n-3)}..."
  }

}
