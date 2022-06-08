package translator

import translator.AssignationsHandler.{makeAssignationsCompact, makeAssignationsExplicit}
import translator.NamesFinder.{allModifiedVars, allReferencedNames}
import translator.TypeInferrer.tryToInferType

import scala.annotation.tailrec
import scala.meta.Term.{Block, While}
import scala.meta.{Decl, Defn, Enumerator, Lit, Pat, Source, Stat, Term, Transformer, Tree, Type}

/**
 * Translator to convert imperative Scala code into its functional equivalent
 *
 * @param translationConfigurationChecker enforces some restrictions about the input program
 * @param reporter                        to report errors
 */
class Translator(translationConfigurationChecker: RestrictionsEnforcer, reporter: Reporter) {
  require(translationConfigurationChecker != null)
  require(reporter != null)

  /**
   * Translates the program contained in `source` to functional style
   *
   * @param source the program to be converted
   * @return the converted program if no error occured, the original one otherwise
   */
  def translateTopLevelOfSource(source: Source): Source = {
    if (translationConfigurationChecker.checkCanConvert(source)) {
      try {
        val transformation =
          (makeAssignationsExplicit: Tree => Tree)
            .andThen { case Source(stats) => stats }
            .andThen(translateStatsSequence(NamingContext.empty, _)(new DisambigIndices()))
            .andThen(_.stats)
            .andThen(Source(_))
            .andThen(Inliner.inlineInStatSequences)
            .andThen(makeAssignationsCompact)
            .andThen(_.asInstanceOf[Source])
        transformation(source)
      } catch {
        case TranslatorException(msg) =>
          reporter.addErrorMsg(msg)
          source
      }
    }
    else source
  }

  /**
   * Translates the `method` to functional style
   *
   * @param method the method to be converted
   * @return the converted method if no error occured, the original one otherwise
   */
  def translateMethod(method: Defn.Def): Defn.Def = {
    if (translationConfigurationChecker.checkCanConvert(method)) {
      try {
        val transformation =
          (makeAssignationsExplicit: Tree => Tree)
            .andThen(_.asInstanceOf[Defn.Def])
            .andThen(translateMethodDef)
            .andThen(Inliner.inlineInStatSequences)
            .andThen(makeAssignationsCompact)
            .andThen(_.asInstanceOf[Defn.Def])
        transformation(method)
      } catch {
        case TranslatorException(msg) =>
          reporter.addErrorMsg(s"Cannot translate method ${method.name.value}: $msg")
          method
      }
    }
    else method
  }

  /**
   * Translates all the methods in the tree (except the ones on which translation fails)
   */
  def translateMethodsIn(tree: Tree): Tree = methodTransformer.apply(tree)

  // used by translateMethodsIn
  private val methodTransformer = new Transformer {
    override def apply(tree: Tree): Tree = tree match {
      case method: Defn.Def => translateMethod(method) // ignore children
      case other => super.apply(other)
    }
  }

  /**
   * Partial result of the translation
   *
   * @param stats         the translated stats
   * @param namingContext the NamingContext at the end of the translation of the list of stats
   */
  private case class TranslationPartRes(stats: List[Stat], namingContext: NamingContext) {
    def withNewStat(tree: Stat): TranslationPartRes = copy(stats = stats :+ tree)
  }

  /**
   * @return the translated method if it succeeds, the original one otherwise
   */
  private def translateMethodDef(defnDef: Defn.Def): Defn.Def = {
    try {
      val namingContext = defnDef.paramss.flatten.foldLeft(NamingContext.empty)(
        (ctx, param) => ctx.updatedWithVal(param.name.value, param.decltpe)
      )
      val di = new DisambigIndices()
      defnDef.copy(body = translateBlockOrUniqueStat(namingContext, defnDef.body)(di).asInstanceOf[Term])
    } catch {
      case TranslatorException(msg) =>
        reporter.addErrorMsg(msg)
        defnDef
    }
  }

  private def translateStatsSequence(namingContext: NamingContext, stats: List[Stat])(implicit di: DisambigIndices): TranslationPartRes = {
    translateStatsFrom(TranslationPartRes(Nil, namingContext), previousStats = Nil, remStats = stats)
  }

  private def translateBlockOrUniqueStat(namingContext: NamingContext, stat: Stat)(implicit di: DisambigIndices): Stat = {
    asBlockOrUniqueStat(translateStatsSequence(namingContext, makeStatsList(stat)).stats)
  }

  /**
   * Translates the remaining statements (`remStats`), but is aware of the previous ones of the same scope (`previousStats`)
   *
   * More precisely, calls the appropriate method to translate the head of `remStats` (depending on its type) and then
   * recursively translates the remaining ones
   */
  @tailrec private def translateStatsFrom(
                                           initPartRes: TranslationPartRes,
                                           previousStats: List[Stat],
                                           remStats: List[Stat]
                                         )(implicit di: DisambigIndices): TranslationPartRes = {
    remStats match {
      case Nil => initPartRes
      case head :: tail => {
        val namingContext = initPartRes.namingContext
        val headTranslationRes = head match {

          case methodDef: Defn.Def => initPartRes.withNewStat(translateMethodDef(methodDef))

          case Defn.Var(mods, List(Pat.Var(Term.Name(nameStr))), tpeOpt, Some(rhs)) => {
            val tpe = tpeOpt.getOrElse(
              tryToInferType(rhs, namingContext).getOrElse(throw TranslatorException(s"cannot infer type of $rhs"))
            )
            val newValDefn = Defn.Val(
              mods = mods,
              pats = List(Pat.Var(namingContext.disambiguatedNameForVar(nameStr))),
              decltpe = tpeOpt,
              rhs = translateTerm(namingContext, rhs)
            )
            TranslationPartRes(initPartRes.stats :+ newValDefn, namingContext.updatedWithVar(nameStr, tpe))
          }

          case varDef@Defn.Var(_, terms, _, _) if terms.size > 1 =>
            throw TranslatorException(s"not supported: $varDef")

          case defnVar: Defn.Var => throw new AssertionError(defnVar.toString())

          case varDecl: Decl.Var => throw TranslatorException(s"variables must be initialized when declared: $varDecl")

          case valDefn@Defn.Val(_, List(Pat.Var(Term.Name(nameStr))), optType, rhs) =>
            val inferredTypeOpt = optType.orElse(tryToInferType(rhs, namingContext))
            TranslationPartRes(
              initPartRes.stats :+ valDefn.copy(rhs = translateTerm(namingContext, rhs)),
              initPartRes.namingContext.updatedWithVal(nameStr, inferredTypeOpt)
            )

          case Term.Assign(Term.Name(nameStr), rhs) if namingContext.currentlyReferencedVars.contains(nameStr) =>
            val newNamingCtx = namingContext.updatedAlreadyExistingVar(nameStr)
            val newName = newNamingCtx.disambiguatedNameForVar(nameStr)
            val valDefn = Defn.Val(
              mods = Nil,
              pats = List(Pat.Var(newName)),
              decltpe = None,
              rhs = renameWhereNeeded(rhs, initPartRes.namingContext).asInstanceOf[Term]
            )
            TranslationPartRes(initPartRes.stats :+ valDefn, newNamingCtx)

          case whileLoop: While => translateWhile(initPartRes, whileLoop, statsAfterWhile = tail)

          case ifStat: Term.If => translateIf(initPartRes, ifStat)

          case patMat: Term.Match => translatePatternMatch(initPartRes, patMat)

          case forDo: Term.For => translateForDo(initPartRes, forDo, remStats = tail)

          case forYield @ Term.ForYield(_, body) =>
            if (allModifiedVars(List(body)).isEmpty) {
              initPartRes.withNewStat(forYield.copy(body = translateTerm(namingContext, body)))
            }
            else throw TranslatorException("for-yield expressions are only supported if no external var is updated in their body")

          case func@Term.Function(_, body) =>
            if (allModifiedVars(List(func)).isEmpty){
              initPartRes.withNewStat(func.copy(body = translateTerm(namingContext, body)))
            }
            else throw TranslatorException("higher-order functions are only supported if no external var is updated in their body")

          case Term.Apply(fun, args) => initPartRes.withNewStat(
            Term.Apply(translateTerm(namingContext, fun), args.map(translateTerm(namingContext, _)))
          )

          case other => initPartRes.copy(
            stats = initPartRes.stats :+ renameWhereNeeded(other, initPartRes.namingContext).asInstanceOf[Stat]
          )
        }
        translateStatsFrom(headTranslationRes, previousStats :+ head, tail)
      }
    }
  }

  private def translateTerm(namingContext: NamingContext, expr: Term)(implicit di: DisambigIndices): Term = {
    val statsList = makeStatsList(expr)
    val transformed = translateStatsSequence(namingContext, statsList)
    asBlockOrUniqueStat(transformed.stats).asInstanceOf[Term]
  }

  private def renameWhereNeeded(tree: Tree, namingContext: NamingContext): Tree = {
    tree.transform {
      case Term.Name(nameStr) => namingContext.disambiguatedNameForVar(nameStr)
    }
  }

  private def makeStatsList(stat: Stat): List[Stat] = {
    stat match {
      case block: Block => block.stats
      case other => List(other)
    }
  }

  private def asBlockOrUniqueStat(stats: List[Stat]): Stat = {
    stats match {
      case List(stat) => stat
      case _ => Block(stats)
    }
  }

  private def makeValPat(varsAsStr: List[String], context: NamingContext): List[Pat] = {
    require(varsAsStr.nonEmpty)
    List(
      if (varsAsStr.size == 1) Pat.Var(context.disambiguatedNameForVar(varsAsStr.head))
      else Pat.Tuple(varsAsStr.map(context.disambiguatedNameForVar).map(Pat.Var(_)))
    )
  }

  private def translateWhile(
                              initPartRes: TranslationPartRes,
                              whileLoop: While,
                              statsAfterWhile: List[Stat]
                            )(implicit di: DisambigIndices): TranslationPartRes = {
    val bodyAsStatsList = makeStatsList(whileLoop.body)
    val untypedMethodArgsSet = allModifiedVars(bodyAsStatsList)
    val namesReferencedInRestOfThisScope = allReferencedNames(statsAfterWhile)
    val argsThatNeedToBeReturned = untypedMethodArgsSet.intersect(namesReferencedInRestOfThisScope).toList
    val methodArgsAsUniqueNames = untypedMethodArgsSet
      .toList
      .flatMap(initPartRes.namingContext.currentlyReferencedVars.get)
    val methodName = di.getAndIncrementAutoGenMethodName()
    val callToAutoGeneratedMethod = Term.Apply(methodName, methodArgsAsUniqueNames.map(_.toDisambiguatedName))
    val blockTranslationRes = translateStatsSequence(initPartRes.namingContext, bodyAsStatsList)
    val methodBody = Term.If(
      cond = whileLoop.expr,
      thenp = Block(
        blockTranslationRes.stats
          :+ renameWhereNeeded(callToAutoGeneratedMethod, blockTranslationRes.namingContext).asInstanceOf[Term]
      ),
      elsep = retValFor(argsThatNeedToBeReturned)
    )
    val methodDef = Defn.Def(
      mods = Nil,
      name = methodName,
      tparams = Nil,
      paramss = List(
        methodArgsAsUniqueNames
          .map(varInfo =>
            Term.Param(mods = Nil, name = varInfo.toDisambiguatedName, decltpe = Some(varInfo.typ), default = None)
          )
      ),
      decltpe = Some(tupleTypeFor(argsThatNeedToBeReturned.map(initPartRes.namingContext.currentlyReferencedVars))),
      body = Block(List(methodBody))
    )
    val ctxAfterExternalCall = argsThatNeedToBeReturned.foldLeft(initPartRes.namingContext)(_.updatedAlreadyExistingVar(_))
    lazy val externalVal = Defn.Val(Nil, makeValPat(argsThatNeedToBeReturned, ctxAfterExternalCall), None, callToAutoGeneratedMethod)
    TranslationPartRes(
      initPartRes.stats ++ List(methodDef, if (argsThatNeedToBeReturned.nonEmpty) externalVal else callToAutoGeneratedMethod),
      ctxAfterExternalCall
    )
  }

  private def translateIf(initPartRes: TranslationPartRes, ifStat: Term.If)(implicit di: DisambigIndices): TranslationPartRes = {
    val thenStatsList = makeStatsList(ifStat.thenp)
    val elseStatsList = makeStatsList(ifStat.elsep)
    val thenPartRes = translateStatsSequence(initPartRes.namingContext, thenStatsList)
    val elsePartRes = translateStatsSequence(initPartRes.namingContext, elseStatsList)
    val modifVars = (allModifiedVars(thenStatsList) ++ allModifiedVars(elseStatsList)).toList
    val transformedIfStat = Term.If(
      cond = renameWhereNeeded(ifStat.cond, initPartRes.namingContext).asInstanceOf[Term],
      thenp = asBlockOrUniqueStat(thenPartRes.stats ++ retValOrNone(modifVars, thenPartRes.namingContext)).asInstanceOf[Term],
      elsep = asBlockOrUniqueStat(elsePartRes.stats ++ retValOrNone(modifVars, elsePartRes.namingContext)).asInstanceOf[Term]
    )
    val namingCtxAfterIf = modifVars.foldLeft(initPartRes.namingContext)(_.updatedAlreadyExistingVar(_))
    lazy val externalVal = Defn.Val(mods = Nil, pats = makeValPat(modifVars, namingCtxAfterIf), decltpe = None, rhs = transformedIfStat)
    initPartRes
      .withNewStat(if (modifVars.nonEmpty) externalVal else transformedIfStat)
      .copy(namingContext = namingCtxAfterIf)
  }

  private def translatePatternMatch(initPartRes: TranslationPartRes, patMat: Term.Match)(implicit di: DisambigIndices): TranslationPartRes = {
    val statsListPerBranch = patMat.cases.map(cse => makeStatsList(cse.body))
    val partResPerBranch = statsListPerBranch.map(statsLs => translateStatsSequence(initPartRes.namingContext, statsLs))
    val modifVars = statsListPerBranch.foldLeft(Set.empty[String])(_ ++ allModifiedVars(_)).toList
    val retTuplePerBranch = partResPerBranch.map(branchPartRes => retValOrNone(modifVars, branchPartRes.namingContext))
    val newCasesBodies = partResPerBranch
      .zip(retTuplePerBranch)
      .map(prRt => asBlockOrUniqueStat(prRt._1.stats ++ prRt._2).asInstanceOf[Term])
    val newCases = patMat.cases
      .zip(newCasesBodies)
      .map(patAndBody =>
        patAndBody._1.copy(
          cond = patAndBody._1.cond.map(translateTerm(initPartRes.namingContext, _)),
          body = patAndBody._2
        )
      )
    val transformedPatMat = Term.Match(
      expr = renameWhereNeeded(patMat.expr, initPartRes.namingContext).asInstanceOf[Term],
      cases = newCases
    )
    val namingCtxAfterPatMatch = modifVars.foldLeft(initPartRes.namingContext)(_.updatedAlreadyExistingVar(_))
    lazy val externalVal = Defn.Val(mods = Nil, pats = makeValPat(modifVars, namingCtxAfterPatMatch), decltpe = None, rhs = transformedPatMat)
    initPartRes
      .withNewStat(if (modifVars.nonEmpty) externalVal else transformedPatMat)
      .copy(namingContext = namingCtxAfterPatMatch)
  }

  /**
   * @return a pair (var, while) where while is the loop and var the iterable updated in the loop
   *
   *         Converts
   * {{{
   *   for (i <- ls if predicate(i)){
   *     println(i)
   *   }
   * }}}
   *         into
   * {{{
   *   var iterator_0 = ls
   *   while (iterator_0.nonEmpty){
   *     val i = ls.head
   *     if (predicate(i)){
   *       println(i)
   *     }
   *     iterator_0 = iterator_0.tail
   *   }
   * }}}
   */
  private def transformImperativeForIntoWhile(forDo: Term.For)(implicit di: DisambigIndices): (Defn.Var, While) = {

    def flattenEnumerators(enumerators: List[Enumerator]): List[Stat] = {
      enumerators match {
        case head :: tail =>
          head match {
            case Enumerator.Generator(pat, rhs) =>
              val iterName = di.getAndIncrementIterableName()
              List(
                Defn.Var(mods = Nil, pats = List(Pat.Var(iterName)), decltpe = None, rhs = Some(rhs)),
                While(
                  expr = Term.Select(iterName, Term.Name("nonEmpty")),
                  body = Block(
                    Defn.Val(mods = Nil, pats = List(pat), decltpe = None, rhs = Term.Select(iterName, Term.Name("head")))
                      +: flattenEnumerators(tail)
                      :+ Term.Assign(iterName, Term.Select(iterName, Term.Name("tail")))
                  )
                )
              )
            case Enumerator.Guard(term) => List(Term.If(cond = term, thenp = Block(flattenEnumerators(tail)), elsep = Lit.Unit()))
            case _ => throw new AssertionError(head.toString())
          }
        case Nil => forDo.body match {
          case Block(stats) => stats
          case _ => List(forDo.body)
        }
      }
    }

    val resAsList = flattenEnumerators(forDo.enums)
    assert(resAsList.size == 2)
    (resAsList.head.asInstanceOf[Defn.Var], resAsList(1).asInstanceOf[While])
  }

  private def translateForDo(
                              initPartRes: TranslationPartRes,
                              forDo: Term.For,
                              remStats: List[Stat]
                            )(implicit di: DisambigIndices): TranslationPartRes = {
    val (varDefn, whileLoop) = transformImperativeForIntoWhile(forDo)
    val varTranslationRes = translateStatsSequence(initPartRes.namingContext, List(varDefn))
    val partResAfter = translateWhile(varTranslationRes, whileLoop, remStats)
    TranslationPartRes(initPartRes.stats ++ partResAfter.stats, initPartRes.namingContext.mergedWith(partResAfter.namingContext))
  }

  private def retValFor(modifVarsStr: List[String]): Term = {
    modifVarsStr match {
      case Nil => Lit.Unit()
      case List(retV) => Term.Name(retV)
      case _ => Term.Tuple(modifVarsStr.map(Term.Name(_)))
    }
  }

  private def retValOrNone(modifVars: List[String], namingContext: NamingContext): Option[Term] = {
    modifVars match {
      case Nil => None
      case List(uniqueVar) => Some(namingContext.disambiguatedNameForVar(uniqueVar))
      case _ => Some(Term.Tuple(modifVars.map(namingContext.disambiguatedNameForVar)))
    }
  }

  private def tupleTypeFor(modifVars: List[VarInfo]): Type = {
    modifVars match {
      case Nil => Type.Name("Unit")
      case List(tpe) => tpe.typ
      case _ => Type.Tuple(modifVars.map(_.typ))
    }
  }

}

object Translator {

  def apply(reporter: Reporter): Translator = new Translator(new RestrictionsEnforcer(reporter), reporter)

}
