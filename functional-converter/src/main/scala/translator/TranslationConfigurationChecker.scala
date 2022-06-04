package translator

import syntactic.CheckResult
import syntactic.whitelist.{WhitelistChecker, Features => F}

import scala.annotation.tailrec
import scala.meta.{Case, Decl, Defn, Name, Pat, Term, Tree}

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

    val methodName = extractMethodName(tree)

    def checkMayBeDisambiguatedTo(initName: String, potentialDisambig: String): Boolean = {
      val sp = potentialDisambig.split("[_]")
      val result = sp.size == 2 && sp.head == initName && sp(1).forall(_.isDigit)
      if (result){
        reporter.addErrorMsg(
          s"Cannot convert method $methodName: disambiguation of $initName may create a name conflict with $potentialDisambig"
        )
      }
      result
    }

    def checkConflict(name1: String, name2: String): Boolean = {
      if (name1 == name2){
        reporter.addErrorMsg(s"Cannot convert method $methodName: identifier $name1 is used more than once")
        true
      }
      else {
        val res1 = checkMayBeDisambiguatedTo(name1, name2)
        val res2 = checkMayBeDisambiguatedTo(name2, name1)
        res1 || res2
      }
    }

    val definedNames = tree.collect {
      case v: Defn.Val => findDeclarations(v.pats)
      case v: Defn.Var => findDeclarations(v.pats)
      case v: Decl.Val => findDeclarations(v.pats)
      case v: Decl.Var => findDeclarations(v.pats)
      case p: Term.Param => List(p.name.value)
      case Case(Pat.Extract(_, pats), _, _) =>
        assert(pats.forall(_.isInstanceOf[Pat.Var]))
        pats.map { case Pat.Var(Term.Name(nameStr)) => nameStr }
      case cse: Case => throw new AssertionError(s"unexpected: ${cse.pat.structure}")
    }.flatten

    !allPairs(definedNames).map(names => checkConflict(names._1, names._2)).exists(identity)
  }

  @tailrec
  private def allPairs(rem: List[String], acc: List[(String, String)] = Nil): List[(String, String)] = {
    rem match {
      case Nil => acc
      case head :: tail => allPairs(tail, acc ++ (for (str <- tail) yield (head, str)))
    }
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
    else s"${str.take(n - 3)}..."
  }

}
