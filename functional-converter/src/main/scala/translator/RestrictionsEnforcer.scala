package translator

import syntactic.CheckResult
import syntactic.whitelist.Feature.AtomicFeature
import syntactic.whitelist.{WhitelistChecker, PredefFeatures => F}

import scala.annotation.tailrec
import scala.meta.{Case, Decl, Defn, Mod, Name, Pat, Term, Tree}

/**
 * The translator cannot translate any Scala programs, some restrictions apply. The ones that are
 * checked before translation starts are checked by a RestrictionEnforcer
 */
class RestrictionsEnforcer(reporter: Reporter) {
  require(reporter != null)

  private object AllowedImperativeConstructs extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.VarParam() => true
      case _: Decl.Var => true
      case _: Defn.Var => true
      case _: Term.Assign => true
      case _: Term.Do => true
      case _: Term.Throw => true
      case _: Term.Try => true
      case _: Term.TryWithHandler => true
      case _: Term.While => true
    }
  }

  // checker for the Scala subset that can be translated
  private val checker = WhitelistChecker(
    F.Vals,F.Defs, F.Nulls, F.LiteralsAndExpressions,
    F.ForExpr, AllowedImperativeConstructs,
    F.PolymorphicTypes, F.StringInterpolation, F.AdvancedOop,
    F.LiteralFunctions
  )

  /**
   * Checks whether the tree does not violate the following restrictions:
   *  - Only a subset of Scala features is allowed
   *  - No two distinct vals/vars can have the same name in the same method
   *  - Names that may conflict after transformation are not allowed (e.g. `x` vs `x_1`)
   * Writes the errors to the error reporter
   * @return true iff the program contained in the given tree does not violate the restrictions
   */
  def checkCanConvert(tree: Tree): Boolean =
    checkThatUsedConstructsAllowConversion(tree) && checkThatValAndVarNamesAllowConversion(tree)

  // check that the tree contains only features from the allowed subset
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

    val methodName = extractMethodName(tree)

    def findDeclarations(pats: List[Pat]): List[String] = pats.flatMap(_.collect { case Name(nameStr) => nameStr })

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

    // use map and then exists to avoid stopping at the first conflict because they must be written in the reporter
    !allPairs(definedNames).map(names => checkConflict(names._1, names._2)).exists(identity)
  }

  /**
   * Creates the list of all pairs (a, b) of elements of rem s.t. a appears before b in rem
   */
  @tailrec private def allPairs(rem: List[String], acc: List[(String, String)] = Nil): List[(String, String)] = {
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
