package translator

import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec
import scala.meta.Term.{Block, While}
import scala.meta.{Decl, Defn, Enumerator, Lit, Pat, Source, Stat, Term, Tree, Type}

class Translator(translationConfigurationChecker: TranslationConfigurationChecker, reporter: Reporter) {
  require(translationConfigurationChecker != null)
  require(reporter != null)

  // TODO pattern match
  // TODO return
  // TODO use vals to avoid naming conflicts

  def translateSource(source: Source): Source = {
    Source(translateStatsSequence(NamingContext.empty, makeAssignationsExplicit(source).asInstanceOf[Source].stats).treesAsStats)
  }

  def translateMethod(method: Defn.Def): Defn.Def = translateFunDef(makeAssignationsExplicit(method).asInstanceOf[Defn.Def])

  private case class TranslaterException(msg: String) extends Exception(msg)

  private case class UnexpectedCaseException(obj: Any) extends Exception(s"unexpected: $obj")

  private case class VarInfo(rawName: String, disambigIdx: Int, typ: Type) {
    def toDisambiguatedName: Term.Name =
      Term.Name(rawName + (if (disambigIdx == 0) "" else automaticNumerotationMarker + disambigIdx))
  }

  private object AutoGenMethodNameGenerator {
    val counter = new AtomicLong(0)

    def nextMethodName: Term.Name = Term.Name(s"autoGen$automaticNumerotationMarker${counter.getAndIncrement()}")
  }

  private object AutoGenIterableNameGenerator {
    val counter = new AtomicLong(0)

    def nextIteratorName: Term.Name = Term.Name(s"autoGenIter$automaticNumerotationMarker${counter.getAndIncrement()}")
  }

  private case class NamingContext(currentlyReferencedVars: Map[String, VarInfo], currentlyReferencedVals: Map[String, Option[Type]]) {
    def updatedWithVar(name: String, tpe: Type): NamingContext = {
      val idxAndTypeForName = currentlyReferencedVars.getOrElse(name, VarInfo(name, -1, tpe))
      NamingContext(
        currentlyReferencedVars.updated(name, idxAndTypeForName.copy(disambigIdx = idxAndTypeForName.disambigIdx + 1)),
        currentlyReferencedVals
      )
    }

    def updatedAlreadyExistingVar(name: String): NamingContext = {
      updatedWithVar(name, currentlyReferencedVars(name).typ)
    }

    def disambiguatedNameForVar(name: String): Term.Name =
      currentlyReferencedVars.get(name).map(_.toDisambiguatedName).getOrElse(Term.Name(name))

    def updatedWithVal(name: String, optType: Option[Type]): NamingContext =
      copy(currentlyReferencedVals = currentlyReferencedVals.updated(name, optType))

    def mergedWith(that: NamingContext): NamingContext =
      NamingContext(currentlyReferencedVars ++ that.currentlyReferencedVars, currentlyReferencedVals ++ that.currentlyReferencedVals)
  }

  private object NamingContext {
    val empty: NamingContext = NamingContext(Map.empty, Map.empty)
  }

  private case class TranslationPartRes(trees: List[Tree], namingContext: NamingContext) {
    def treesAsStats: List[Stat] = trees.map(_.asInstanceOf[Stat])

    def withNewTree(tree: Tree): TranslationPartRes = copy(trees = trees :+ tree)
  }

  private object TranslationPartRes {
    val empty: TranslationPartRes = TranslationPartRes(Nil, NamingContext.empty)
  }

  private def takeUntilReturn(stats: List[Stat]): List[Stat] = {
    val retFree = stats.takeWhile(!_.isInstanceOf[Term.Return])
    if (retFree.size == stats.size) stats
    else if (retFree.size == stats.size - 1) retFree :+ stats(retFree.size).asInstanceOf[Term.Return].expr
    else retFree :+ stats(retFree.size).asInstanceOf[Term.Return] //.expr
    // FIXME handle return
  }

  private def translateFunDef(defnDef: Defn.Def): Defn.Def = {
    try {
      defnDef.copy(body = translateBlockOrUniqueStat(NamingContext.empty, defnDef.body).asInstanceOf[Term])
    } catch {
      case e: TranslaterException =>
        reporter.addErrorMsg(e.msg)
        defnDef
    }
  }

  private def translateStatsSequence(namingContext: NamingContext, stats: List[Stat]): TranslationPartRes = {
    val meaningfulStats = takeUntilReturn(stats)
    translateStatsFrom(TranslationPartRes(Nil, namingContext), previousStats = Nil, remStats = meaningfulStats)
  }

  private def translateBlockOrUniqueStat(namingContext: NamingContext, stat: Stat): Stat = {
    asBlockOrUniqueStat(translateStatsSequence(namingContext, makeStatsList(stat)).treesAsStats)
  }

  @tailrec
  private def translateStatsFrom(initPartRes: TranslationPartRes, previousStats: List[Stat], remStats: List[Stat]): TranslationPartRes = {
    remStats match {
      case Nil => initPartRes
      case head :: tail => {
        val namingContext = initPartRes.namingContext
        val headTranslationRes = head match {
          case funDef: Defn.Def => initPartRes.withNewTree(translateFunDef(funDef))
          case varDefn@Defn.Var(mods, List(Pat.Var(Term.Name(nameStr))), tpeOpt, Some(rhs)) => {
            val tpe = tpeOpt match {
              case Some(t) => t
              case None =>
                tryToInferType(rhs, namingContext) match {
                  case Some(t) => t
                  case None => throw TranslaterException(s"cannot infer type of $rhs")
                }
            }
            val newValDefn = Defn.Val(mods, List(Pat.Var(namingContext.disambiguatedNameForVar(nameStr))), tpeOpt, rhs)
            TranslationPartRes(initPartRes.trees :+ newValDefn, namingContext.updatedWithVar(nameStr, tpe))
          }
          case varDef@Defn.Var(mods, terms, tpe, rhs) if terms.size > 1 =>
            throw TranslaterException(s"not supported: $varDef")
          case defnVar@Defn.Var(mods, terms, tpe, rhs) => {
            throw UnexpectedCaseException(defnVar)
          }
          case _: Decl.Var => throw TranslaterException("variables must be initialized when declared")
          case valDefn@Defn.Val(mods, List(Pat.Var(Term.Name(nameStr))), optType, rhs) => {
            val inferredTypeOpt = optType match {
              case t@Some(_) => t
              case None => tryToInferType(rhs, namingContext)
            }
            TranslationPartRes(initPartRes.trees :+ valDefn, initPartRes.namingContext.updatedWithVal(nameStr, inferredTypeOpt))
          }
          case assig@Term.Assign(name@Term.Name(nameStr), rhs) if namingContext.currentlyReferencedVars.contains(nameStr) => {
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
          case whileLoop: While => translateWhile(initPartRes, whileLoop, previousStats = previousStats, remStats = tail)
          case ifStat: Term.If => translateIf(initPartRes, ifStat)
          case forDo: Term.For => translateForDo(initPartRes, forDo)
          case forYield: Term.ForYield =>
            if (allModifiedVars(List(forYield)).isEmpty) initPartRes.copy(trees = initPartRes.trees :+ forYield)
            else throw TranslaterException("for-yield expressions are only supported if no var is updated in their body")
          case patMat: Term.Match => throw TranslaterException("pattern match is not supported") // FIXME
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
                              previousStats: List[Stat],
                              remStats: List[Stat]
                            ): TranslationPartRes = {
    val bodyAsBlock = makeStatsList(whileLoop.body)
    val untypedMethodArgsSet = allModifiedVars(bodyAsBlock)
    val untypedMethodArgs = untypedMethodArgsSet.toList
    val previouslyDeclaredInThisScope = allDeclaredVars(previousStats)
    val referencedInRestOfThisScope = allReferencedVars(remStats)
    val needingToBeReturned = untypedMethodArgsSet.diff(previouslyDeclaredInThisScope.diff(referencedInRestOfThisScope)).toList
    val methodArgsAsUniqueNames = untypedMethodArgs
      .flatMap(initPartRes.namingContext.currentlyReferencedVars.get)
    val methodParams = methodArgsAsUniqueNames
      .map(u => Term.Param(mods = Nil, name = u.toDisambiguatedName, decltpe = Some(u.typ), default = None))
    val tupleReturnType = tupleTypeFor(needingToBeReturned.map(initPartRes.namingContext.currentlyReferencedVars))
    val methodName = AutoGenMethodNameGenerator.nextMethodName
    val methodCall = Term.Apply(methodName, methodArgsAsUniqueNames.map(_.toDisambiguatedName))
    val blockTranslationRes = translateStatsSequence(initPartRes.namingContext, bodyAsBlock)
    val retVal = tupleFor(needingToBeReturned)
    val thenBranch = Block(
      blockTranslationRes.treesAsStats
        :+ renameWhereNeeded(methodCall, blockTranslationRes.namingContext).asInstanceOf[Term]
    )
    val methodBody = Term.If(whileLoop.expr, thenBranch, retVal)
    val methodDef = Defn.Def(
      mods = Nil,
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

  private def translateIf(initPartRes: TranslationPartRes, ifStat: Term.If): TranslationPartRes = {
    val thenPartRes = translateStatsSequence(initPartRes.namingContext, makeStatsList(ifStat.thenp))
    val elsePartRes = translateStatsSequence(initPartRes.namingContext, makeStatsList(ifStat.elsep))
    val modifVars = (allModifiedVars(makeStatsList(ifStat.thenp)) ++ allModifiedVars(makeStatsList(ifStat.elsep))).toList
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

  private def forDoIntoWhile(forDo: Term.For): List[Stat] = {

    def flattenEnumerator(enumerators: List[Enumerator]): List[Stat] = {
      enumerators match {
        case head :: tail =>
          head match {
            case Enumerator.Generator(pat, rhs) =>
              val iterName = AutoGenIterableNameGenerator.nextIteratorName
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
            case _ => throw UnexpectedCaseException(head)
          }
        case Nil => forDo.body match {
          case Block(stats) => stats
          case _ => List(forDo.body).map(_.asInstanceOf[Stat])
        }
      }
    }

    flattenEnumerator(forDo.enums)
  }

  private def translateForDo(initPartRes: TranslationPartRes, forDo: Term.For): TranslationPartRes = {
    val partResAfter = translateStatsSequence(initPartRes.namingContext, forDoIntoWhile(forDo))
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

  private def inlineWherePossible(stats: List[Stat]): List[Stat] = {
    // TODO inline args
    if (stats.size < 2) stats
    else {
      stats.slice(stats.size - 2, stats.size) match {
        case List(Defn.Val(mods, List(Pat.Var(Term.Name(name1))), optType, rhs), Term.Name(name2)) if name1 == name2 =>
          stats.slice(0, stats.size - 2) :+ rhs
        case _ => stats
      }
    }
  }

  /////// Basic type inference /////////////////////////////////////////////////////////////////////////////////

  private def tryToInferType(expr: Term, namingContext: NamingContext): Option[Type] = {
    expr match {
      case Term.Name(nameStr) if namingContext.currentlyReferencedVals.contains(nameStr) =>
        namingContext.currentlyReferencedVals(nameStr)
      case Term.Name(nameStr) if namingContext.currentlyReferencedVars.contains(nameStr) =>
        namingContext.currentlyReferencedVars.get(nameStr).map(_.typ)
      case _: Lit.Int => Some(Type.Name("Int"))
      case _: Lit.Boolean => Some(Type.Name("Boolean"))
      case _: Lit.String => Some(Type.Name("String"))
      case _: Lit.Unit => Some(Type.Name("Unit"))
      // TODO more cases
      case _ => None
    }
  }

  /////// Find modified vars ///////////////////////////////////////////////////////////////////////////////////

  private def allModifiedVars(stats: List[Stat]): Set[String] = {
    val updatedVars = allUpdatedVars(stats)
    val declaredVars = allDeclaredVars(stats)
    updatedVars.diff(declaredVars)
  }

  private def allUpdatedVars(stats: List[Stat]): Set[String] = {
    stats.flatMap(stat => {
      stat.collect {
        case Term.Assign(Term.Name(nameStr), _) => Some(nameStr)
        case assign@Term.Assign(_, _) => throw UnexpectedCaseException(assign)
        case _ => None
      }.flatten
    }).toSet
  }

  private def allDeclaredVars(stats: List[Stat]): Set[String] = {
    stats.flatMap(stat => {
      stat.collect {
        case varDef@Defn.Var(mods, pats, optType, optTerm) if pats.size > 1 =>
          throw TranslaterException(s"not supported: $varDef")
        case Defn.Var(mods, List(Pat.Var(Term.Name(nameStr))), optType, optTerm) => Some(nameStr)
        case defnVar: Defn.Var => throw UnexpectedCaseException(defnVar)
        case _ => None
      }.flatten
    }).toSet
  }

  private def allReferencedVars(stats: List[Stat]): Set[String] = {
    stats.flatMap(stat => {
      stat.collect {
        case Term.Name(nameStr) => Some(nameStr)
        case _ => None
      }.flatten
    }).toSet
  }

  /////// += , -= , *= , etc. //////////////////////////////////////////////////////////////////////////////////

  private def makeAssignationsExplicit(tree: Tree): Tree = {
    tree.transform {
      case Term.ApplyInfix(lhs, Term.Name(op), targs, args)
        if op.length >= 2 && op.last == '=' && assignableOperators.contains(op.dropRight(1))
      =>
        Term.Assign(lhs, Term.ApplyInfix(lhs, Term.Name(op.init), targs, args))
      case anyTree => anyTree
    }
  }

  private def makeAssignationsCompact(tree: Tree): Tree = {
    tree.transform {
      case Term.Assign(lhs@Term.Name(lhs1), Term.ApplyInfix(Term.Name(lhs2), Term.Name(op), targs, args))
        if lhs1 == lhs2 && assignableOperators.contains(op)
      =>
        Term.ApplyInfix(lhs, Term.Name(op + "="), targs, args)
      case anyTree => anyTree
    }
  }

  private val assignableOperators = List("+", "-", "*", "/", "%", "<<", ">>", "^", "&", "|")
  private val automaticNumerotationMarker = "_"

}

object Translator {

  def apply(reporter: Reporter): Translator = new Translator(new TranslationConfigurationChecker(reporter), reporter)

}