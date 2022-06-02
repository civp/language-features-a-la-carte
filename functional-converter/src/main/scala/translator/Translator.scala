package translator

import translator.AssignationsHandler.{makeAssignationsCompact, makeAssignationsExplicit}
import translator.NamesFinder.{allDeclaredVars, allModifiedVars, allReferencedVars}
import translator.TypeInferrer.tryToInferType

import scala.annotation.tailrec
import scala.meta.Term.{Block, While}
import scala.meta.{Decl, Defn, Enumerator, Lit, Pat, Source, Stat, Term, Transformer, Tree, Type}

class Translator(translationConfigurationChecker: TranslationConfigurationChecker, reporter: Reporter) {
  require(translationConfigurationChecker != null)
  require(reporter != null)

  private val methodTransformer = new Transformer {
    override def apply(tree: Tree): Tree = tree match {
      case method: Defn.Def => translateMethod(method)
      case other => super.apply(other)
    }
  }

  def translateTopLevelOfSource(source: Source): Source = {
    if (translationConfigurationChecker.checkCanConvert(source)) {
      try {
        makeAssignationsCompact(Source(
          translateStatsSequence(
            NamingContext.empty, makeAssignationsExplicit(source).asInstanceOf[Source].stats
          )(new DisambigIndices()).treesAsStats
        )).asInstanceOf[Source]
      } catch {
        case TranslaterException(msg) =>
          reporter.addErrorMsg(msg)
          source
      }
    }
    else source
  }

  def translateMethod(method: Defn.Def): Defn.Def = {
    if (translationConfigurationChecker.checkCanConvert(method)) {
      try {
        val treeWithExplicitAssig = makeAssignationsExplicit(method)
        makeAssignationsCompact(translateMethodDef(treeWithExplicitAssig.asInstanceOf[Defn.Def])).asInstanceOf[Defn.Def]
      } catch {
        case TranslaterException(msg) =>
          reporter.addErrorMsg(s"Cannot translate method ${method.name.value}: $msg")
          method
      }
    }
    else method
  }

  def translateMethodsIn(tree: Tree): Tree = methodTransformer.apply(tree)

  private case class TranslationPartRes(trees: List[Tree], namingContext: NamingContext) {
    def treesAsStats: List[Stat] = trees.map(_.asInstanceOf[Stat])

    def withNewTree(tree: Tree): TranslationPartRes = copy(trees = trees :+ tree)
  }

  private def translateMethodDef(defnDef: Defn.Def): Defn.Def = {
    try {
      val namingContext = defnDef.paramss.flatten.foldLeft(NamingContext.empty)(
        (ctx, param) => ctx.updatedWithVal(param.name.value, param.decltpe)
      )
      val di = new DisambigIndices()
      defnDef.copy(body = translateBlockOrUniqueStat(namingContext, defnDef.body)(di).asInstanceOf[Term])
    } catch {
      case TranslaterException(msg) =>
        reporter.addErrorMsg(msg)
        defnDef
    }
  }

  private def translateStatsSequence(namingContext: NamingContext, stats: List[Stat])(implicit di: DisambigIndices): TranslationPartRes = {
    translateStatsFrom(TranslationPartRes(Nil, namingContext), previousStats = Nil, remStats = stats)
  }

  private def translateBlockOrUniqueStat(namingContext: NamingContext, stat: Stat)(implicit di: DisambigIndices): Stat = {
    asBlockOrUniqueStat(translateStatsSequence(namingContext, makeStatsList(stat)).treesAsStats)
  }

  @tailrec
  private def translateStatsFrom(initPartRes: TranslationPartRes, previousStats: List[Stat], remStats: List[Stat])(implicit di: DisambigIndices): TranslationPartRes = {
    remStats match {
      case Nil => initPartRes
      case head :: tail => {
        val namingContext = initPartRes.namingContext
        val headTranslationRes = head match {
          case methodDef: Defn.Def => initPartRes.withNewTree(translateMethodDef(methodDef))
          case Defn.Var(mods, List(Pat.Var(Term.Name(nameStr))), tpeOpt, Some(rhs)) => {
            val tpe = tpeOpt.getOrElse(
              tryToInferType(rhs, namingContext).getOrElse(throw TranslaterException(s"cannot infer type of $rhs"))
            )
            val newValDefn = Defn.Val(mods, List(Pat.Var(namingContext.disambiguatedNameForVar(nameStr))), tpeOpt, rhs)
            TranslationPartRes(initPartRes.trees :+ newValDefn, namingContext.updatedWithVar(nameStr, tpe))
          }
          case varDef@Defn.Var(mods, terms, tpe, rhs) if terms.size > 1 =>
            throw TranslaterException(s"not supported: $varDef")
          case defnVar: Defn.Var => throw UnexpectedConstructException(defnVar)
          case varDecl: Decl.Var => throw TranslaterException(s"variables must be initialized when declared: $varDecl")
          case valDefn@Defn.Val(mods, List(Pat.Var(Term.Name(nameStr))), optType, rhs) => {
            val inferredTypeOpt = optType.orElse(tryToInferType(rhs, namingContext))
            TranslationPartRes(initPartRes.trees :+ valDefn, initPartRes.namingContext.updatedWithVal(nameStr, inferredTypeOpt))
          }
          case Term.Assign(Term.Name(nameStr), rhs) if namingContext.currentlyReferencedVars.contains(nameStr) => {
            val newNamingCtx = namingContext.updatedAlreadyExistingVar(nameStr)
            val newName = newNamingCtx.disambiguatedNameForVar(nameStr)
            val valDefn = Defn.Val(
              mods = Nil,
              pats = List(Pat.Var(newName)),
              decltpe = None,
              rhs = renameWhereNeeded(rhs, initPartRes.namingContext).asInstanceOf[Term]
            )
            TranslationPartRes(initPartRes.trees :+ valDefn, newNamingCtx)
          }
          case whileLoop: While => translateWhile(initPartRes, whileLoop, remStats = tail)
          case ifStat: Term.If => translateIf(initPartRes, ifStat)
          case forDo: Term.For => translateForDo(initPartRes, forDo, remStats = tail)
          case forYield: Term.ForYield =>
            if (allModifiedVars(List(forYield)).isEmpty) initPartRes.copy(trees = initPartRes.trees :+ forYield)
            else throw TranslaterException("for-yield expressions are only supported if no var is updated in their body")
          case patMat: Term.Match => translatePatternMatch(initPartRes, patMat)
          case other => initPartRes.copy(trees = initPartRes.trees :+ renameWhereNeeded(other, initPartRes.namingContext))
        }
        translateStatsFrom(headTranslationRes, previousStats :+ head, tail)
      }
    }
  }

  private def renameWhereNeeded(tree: Tree, namingContext: NamingContext): Tree = {
    tree.transform {
      case Term.Name(nameStr) => namingContext.disambiguatedNameForVar(nameStr)
      case els => els
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
                              remStats: List[Stat]
                            )(implicit di: DisambigIndices): TranslationPartRes = {
    val bodyAsBlock = makeStatsList(whileLoop.body)
    val untypedMethodArgsSet = allModifiedVars(bodyAsBlock)
    val untypedMethodArgs = untypedMethodArgsSet.toList
    val referencedInRestOfThisScope = allReferencedVars(remStats)
    val needingToBeReturned = untypedMethodArgsSet.intersect(referencedInRestOfThisScope).toList
    val methodArgsAsUniqueNames = untypedMethodArgs
      .flatMap(initPartRes.namingContext.currentlyReferencedVars.get)
    val methodParams = methodArgsAsUniqueNames
      .map(u => Term.Param(mods = Nil, name = u.toDisambiguatedName, decltpe = Some(u.typ), default = None))
    val tupleReturnType = tupleTypeFor(needingToBeReturned.map(initPartRes.namingContext.currentlyReferencedVars))
    val methodName = di.incAndGetAutoGenMethodName()
    val methodCall = Term.Apply(methodName, methodArgsAsUniqueNames.map(_.toDisambiguatedName))
    val blockTranslationRes = translateStatsSequence(initPartRes.namingContext, bodyAsBlock)
    val retVal = tupleFor(needingToBeReturned)
    val thenBranch = Block(
      blockTranslationRes.treesAsStats
        :+ renameWhereNeeded(methodCall, blockTranslationRes.namingContext).asInstanceOf[Term]
    )
    val methodBody = Term.If(whileLoop.expr, thenBranch, retVal)
    val methodDef = Defn.Def(
      mods = Nil, // List(Annot(Init(Type.Name("tailrec"), Name.Anonymous(), Nil))),  TODO
      name = methodName,
      tparams = Nil,
      paramss = List(methodParams),
      decltpe = Some(tupleReturnType),
      body = Block(List(methodBody))
    )
    val ctxAfterExternalCall = needingToBeReturned.foldLeft(initPartRes.namingContext)(_.updatedAlreadyExistingVar(_))
    lazy val externalVal = Defn.Val(Nil, makeValPat(needingToBeReturned, ctxAfterExternalCall), None, methodCall)
    TranslationPartRes(
      initPartRes.trees ++ List(methodDef, if (needingToBeReturned.nonEmpty) externalVal else methodCall),
      ctxAfterExternalCall
    )
  }

  private def translateIf(initPartRes: TranslationPartRes, ifStat: Term.If)(implicit di: DisambigIndices): TranslationPartRes = {
    val thenStatsList = makeStatsList(ifStat.thenp)
    val elseStatsList = makeStatsList(ifStat.elsep)
    val thenPartRes = translateStatsSequence(initPartRes.namingContext, thenStatsList)
    val elsePartRes = translateStatsSequence(initPartRes.namingContext, elseStatsList)
    val modifVars = (allModifiedVars(thenStatsList) ++ allModifiedVars(elseStatsList)).toList
    val thenRetTuple = if (modifVars.nonEmpty) Some(Term.Tuple(modifVars.map(thenPartRes.namingContext.disambiguatedNameForVar))) else None
    val elseRetTuple = if (modifVars.nonEmpty) Some(Term.Tuple(modifVars.map(elsePartRes.namingContext.disambiguatedNameForVar))) else None
    val transformedIfStat = ifStat.copy(
      thenp = asBlockOrUniqueStat(thenPartRes.treesAsStats ++ (if (modifVars.nonEmpty) thenRetTuple.toList else Nil)).asInstanceOf[Term],
      elsep = asBlockOrUniqueStat(elsePartRes.treesAsStats ++ (if (modifVars.nonEmpty) elseRetTuple.toList else Nil)).asInstanceOf[Term]
    )
    val namingCtxAfterIf = modifVars.foldLeft(initPartRes.namingContext)(_.updatedAlreadyExistingVar(_))
    lazy val externalVal = Defn.Val(mods = Nil, pats = makeValPat(modifVars, namingCtxAfterIf), decltpe = None, rhs = transformedIfStat)
    initPartRes
      .withNewTree(if (modifVars.nonEmpty) externalVal else transformedIfStat)
      .copy(namingContext = namingCtxAfterIf)
  }

  private def translatePatternMatch(initPartRes: TranslationPartRes, patMat: Term.Match)(implicit di: DisambigIndices): TranslationPartRes = {
    val statsListPerBranch = patMat.cases.map(cse => makeStatsList(cse.body))
    val partResPerBranch = statsListPerBranch.map(statsLs => translateStatsSequence(initPartRes.namingContext, statsLs))
    val modifVars = statsListPerBranch.foldLeft(Set.empty[String])(_ ++ allModifiedVars(_)).toList
    val retTuplePerBranch = partResPerBranch.map(branchPartRes =>
      if (modifVars.nonEmpty) Some(Term.Tuple(modifVars.map(branchPartRes.namingContext.disambiguatedNameForVar))) else None
    )
    val newCasesBodies = partResPerBranch.zip(retTuplePerBranch)
      .map(prRt => asBlockOrUniqueStat(prRt._1.treesAsStats ++ (if (modifVars.nonEmpty) prRt._2.toList else Nil)).asInstanceOf[Term])
    val newCases = patMat.cases.zip(newCasesBodies).map(patAndBody => patAndBody._1.copy(body = patAndBody._2))
    val transformedPatMat = patMat.copy(cases = newCases)
    val namingCtxAfterPatMatch = modifVars.foldLeft(initPartRes.namingContext)(_.updatedAlreadyExistingVar(_))
    lazy val externalVal = Defn.Val(mods = Nil, pats = makeValPat(modifVars, namingCtxAfterPatMatch), decltpe = None, rhs = transformedPatMat)
    initPartRes
      .withNewTree(if (modifVars.nonEmpty) externalVal else transformedPatMat)
      .copy(namingContext = namingCtxAfterPatMatch)
  }

  private def forDoIntoWhile(forDo: Term.For)(implicit di: DisambigIndices): (Defn.Var, While) = {

    def flattenEnumerator(enumerators: List[Enumerator]): List[Stat] = {
      enumerators match {
        case head :: tail =>
          head match {
            case Enumerator.Generator(pat, rhs) =>
              val iterName = di.incAndGetIterableName()
              List(
                Defn.Var(mods = Nil, pats = List(Pat.Var(iterName)), decltpe = None, rhs = Some(rhs)),
                While(
                  expr = Term.Select(iterName, Term.Name("nonEmpty")),
                  body = Block(
                    Defn.Val(mods = Nil, pats = List(pat), decltpe = None, rhs = Term.Select(iterName, Term.Name("head")))
                      +: flattenEnumerator(tail)
                      :+ Term.Assign(iterName, Term.Select(iterName, Term.Name("tail")))
                  )
                )
              )
            case Enumerator.Guard(term) => List(Term.If(cond = term, thenp = Block(flattenEnumerator(tail)), elsep = Lit.Unit()))
            case _ => throw UnexpectedConstructException(head)
          }
        case Nil => forDo.body match {
          case Block(stats) => stats
          case _ => List(forDo.body).map(_.asInstanceOf[Stat])
        }
      }
    }

    val resAsList = flattenEnumerator(forDo.enums)
    assert(resAsList.size == 2)
    (resAsList.head.asInstanceOf[Defn.Var], resAsList(1).asInstanceOf[While])
  }

  private def translateForDo(initPartRes: TranslationPartRes, forDo: Term.For, remStats: List[Stat])(implicit di: DisambigIndices): TranslationPartRes = {
    val (varDefn, whileLoop) = forDoIntoWhile(forDo)
    val varTranslationRes = translateStatsSequence(initPartRes.namingContext, List(varDefn))
    val partResAfter = translateWhile(varTranslationRes, whileLoop, remStats)
    TranslationPartRes(initPartRes.trees ++ partResAfter.trees, initPartRes.namingContext.mergedWith(partResAfter.namingContext))
  }

  private def tupleFor(modifVarsStr: List[String]): Term = {
    modifVarsStr match {
      case Nil => Lit.Unit()
      case List(retV) => Term.Name(retV)
      case _ => Term.Tuple(modifVarsStr.map(Term.Name(_)))
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

  def apply(reporter: Reporter): Translator = new Translator(new TranslationConfigurationChecker(reporter), reporter)

}
