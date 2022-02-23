import Feature._

object Features {

  object AllowAllEssentialFeatures extends CompositeFeature(AllowSources, AllowTemplates, AllowSelfs, AllowNames)
  object AllowAllDefDefinitionFeatures extends CompositeFeature(AllowDefDefns, AllowParamTerms, AllowParamTypes)


  case object AllowValDecls extends AtomicFeature({
    case _ : scala.meta.Decl.Val => true
  })
  case object AllowVarDecls extends AtomicFeature({
    case _ : scala.meta.Decl.Var => true
  })
  case object AllowDefDecls extends AtomicFeature({
    case _ : scala.meta.Decl.Def => true
  })
  case object AllowTypeDecls extends AtomicFeature({
    case _ : scala.meta.Decl.Type => true
  })
  case object AllowValDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Val => true
  })
  case object AllowVarDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Var => true
  })
  case object AllowDefDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Def => true
  })
  case object AllowMacroDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Macro => true
  })
  case object AllowTypeDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Type => true
  })
  case object AllowClassDefnes extends AtomicFeature({
    case _ : scala.meta.Defn.Class => true
  })
  case object AllowTraitDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Trait => true
  })
  case object AllowObjectDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Object => true
  })
  case object AllowEnumDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Enum => true
  })
  case object AllowEnumcaseDefns extends AtomicFeature({
    case _ : scala.meta.Defn.EnumCase => true
  })
  case object AllowRepeatedenumcaseDefns extends AtomicFeature({
    case _ : scala.meta.Defn.RepeatedEnumCase => true
  })
  case object AllowGivenDefns extends AtomicFeature({
    case _ : scala.meta.Defn.Given => true
  })
  case object AllowGivenaliasDefnes extends AtomicFeature({
    case _ : scala.meta.Defn.GivenAlias => true
  })
  case object AllowExtensiongroupDefns extends AtomicFeature({
    case _ : scala.meta.Defn.ExtensionGroup => true
  })
  case object AllowThisTermes extends AtomicFeature({
    case _ : scala.meta.Term.This => true
  })
  case object AllowSuperTerms extends AtomicFeature({
    case _ : scala.meta.Term.Super => true
  })
  case object AllowNameTerms extends AtomicFeature({
    case _ : scala.meta.Term.Name => true
  })
  case object AllowSelectTerms extends AtomicFeature({
    case _ : scala.meta.Term.Select => true
  })
  case object AllowApplyunaryTerms extends AtomicFeature({
    case _ : scala.meta.Term.ApplyUnary => true
  })
  case object AllowApplyTerms extends AtomicFeature({
    case _ : scala.meta.Term.Apply => true
  })
  case object AllowApplytypeTerms extends AtomicFeature({
    case _ : scala.meta.Term.ApplyType => true
  })
  case object AllowApplyinfixTerms extends AtomicFeature({
    case _ : scala.meta.Term.ApplyInfix => true
  })
  case object AllowAssignTerms extends AtomicFeature({
    case _ : scala.meta.Term.Assign => true
  })
  case object AllowReturnTerms extends AtomicFeature({
    case _ : scala.meta.Term.Return => true
  })
  case object AllowThrowTerms extends AtomicFeature({
    case _ : scala.meta.Term.Throw => true
  })
  case object AllowAscribeTerms extends AtomicFeature({
    case _ : scala.meta.Term.Ascribe => true
  })
  case object AllowAnnotateTerms extends AtomicFeature({
    case _ : scala.meta.Term.Annotate => true
  })
  case object AllowTupleTerms extends AtomicFeature({
    case _ : scala.meta.Term.Tuple => true
  })
  case object AllowBlockTerms extends AtomicFeature({
    case _ : scala.meta.Term.Block => true
  })
  case object AllowIfTerms extends AtomicFeature({
    case _ : scala.meta.Term.If => true
  })
  case object AllowMatchTerms extends AtomicFeature({
    case _ : scala.meta.Term.Match => true
  })
  case object AllowTryTerms extends AtomicFeature({
    case _ : scala.meta.Term.Try => true
  })
  case object AllowTrywithhandlerTerms extends AtomicFeature({
    case _ : scala.meta.Term.TryWithHandler => true
  })
  case object AllowFunctionTerms extends AtomicFeature({
    case _ : scala.meta.Term.Function => true
  })
  case object AllowPartialfunctionTerms extends AtomicFeature({
    case _ : scala.meta.Term.PartialFunction => true
  })
  case object AllowWhileTerms extends AtomicFeature({
    case _ : scala.meta.Term.While => true
  })
  case object AllowDoTerms extends AtomicFeature({
    case _ : scala.meta.Term.Do => true
  })
  case object AllowForTerms extends AtomicFeature({
    case _ : scala.meta.Term.For => true
  })
  case object AllowForyieldTerms extends AtomicFeature({
    case _ : scala.meta.Term.ForYield => true
  })
  case object AllowNewTerms extends AtomicFeature({
    case _ : scala.meta.Term.New => true
  })
  case object AllowNewanonymousTermes extends AtomicFeature({
    case _ : scala.meta.Term.NewAnonymous => true
  })
  case object AllowPlaceholderTerms extends AtomicFeature({
    case _ : scala.meta.Term.Placeholder => true
  })
  case object AllowEtaTerms extends AtomicFeature({
    case _ : scala.meta.Term.Eta => true
  })
  case object AllowRepeatedTerms extends AtomicFeature({
    case _ : scala.meta.Term.Repeated => true
  })
  case object AllowParamTerms extends AtomicFeature({
    case _ : scala.meta.Term.Param => true
  })
  case object AllowInterpolateTerms extends AtomicFeature({
    case _ : scala.meta.Term.Interpolate => true
  })
  case object AllowXmlTerms extends AtomicFeature({
    case _ : scala.meta.Term.Xml => true
  })
  case object AllowApplyusingTerms extends AtomicFeature({
    case _ : scala.meta.Term.ApplyUsing => true
  })
  case object AllowEndmarkerTerms extends AtomicFeature({
    case _ : scala.meta.Term.EndMarker => true
  })
  case object AllowQuotedmacroexprTerms extends AtomicFeature({
    case _ : scala.meta.Term.QuotedMacroExpr => true
  })
  case object AllowQuotedmacrotypeTerms extends AtomicFeature({
    case _ : scala.meta.Term.QuotedMacroType => true
  })
  case object AllowSplicedmacroexprTerms extends AtomicFeature({
    case _ : scala.meta.Term.SplicedMacroExpr => true
  })
  case object AllowContextfunctionTerms extends AtomicFeature({
    case _ : scala.meta.Term.ContextFunction => true
  })
  case object AllowPolyfunctionTerms extends AtomicFeature({
    case _ : scala.meta.Term.PolyFunction => true
  })
  case object AllowNameTypes extends AtomicFeature({
    case _ : scala.meta.Type.Name => true
  })
  case object AllowSelectTypes extends AtomicFeature({
    case _ : scala.meta.Type.Select => true
  })
  case object AllowProjectTypes extends AtomicFeature({
    case _ : scala.meta.Type.Project => true
  })
  case object AllowSingletonTypes extends AtomicFeature({
    case _ : scala.meta.Type.Singleton => true
  })
  case object AllowApplyTypes extends AtomicFeature({
    case _ : scala.meta.Type.Apply => true
  })
  case object AllowApplyinfixTypes extends AtomicFeature({
    case _ : scala.meta.Type.ApplyInfix => true
  })
  case object AllowFunctionTypes extends AtomicFeature({
    case _ : scala.meta.Type.Function => true
  })
  case object AllowTupleTypes extends AtomicFeature({
    case _ : scala.meta.Type.Tuple => true
  })
  case object AllowWithTypes extends AtomicFeature({
    case _ : scala.meta.Type.With => true
  })
  case object AllowAndTypes extends AtomicFeature({
    case _ : scala.meta.Type.And => true
  })
  case object AllowOrTypes extends AtomicFeature({
    case _ : scala.meta.Type.Or => true
  })
  case object AllowRefineTypes extends AtomicFeature({
    case _ : scala.meta.Type.Refine => true
  })
  case object AllowExistentialTypes extends AtomicFeature({
    case _ : scala.meta.Type.Existential => true
  })
  case object AllowAnnotateTypes extends AtomicFeature({
    case _ : scala.meta.Type.Annotate => true
  })
  case object AllowLambdaTypes extends AtomicFeature({
    case _ : scala.meta.Type.Lambda => true
  })
  case object AllowPlaceholderTypes extends AtomicFeature({
    case _ : scala.meta.Type.Placeholder => true
  })
  case object AllowBoundsTypees extends AtomicFeature({
    case _ : scala.meta.Type.Bounds => true
  })
  case object AllowBynameTypes extends AtomicFeature({
    case _ : scala.meta.Type.ByName => true
  })
  case object AllowRepeatedTypes extends AtomicFeature({
    case _ : scala.meta.Type.Repeated => true
  })
  case object AllowVarTypes extends AtomicFeature({
    case _ : scala.meta.Type.Var => true
  })
  case object AllowParamTypes extends AtomicFeature({
    case _ : scala.meta.Type.Param => true
  })
  case object AllowPolyfunctionTypes extends AtomicFeature({
    case _ : scala.meta.Type.PolyFunction => true
  })
  case object AllowContextfunctionTypes extends AtomicFeature({
    case _ : scala.meta.Type.ContextFunction => true
  })
  case object AllowMatchTypes extends AtomicFeature({
    case _ : scala.meta.Type.Match => true
  })
  case object AllowLits extends AtomicFeature({
    case _ : scala.meta.Lit => true
  })
  case object AllowWildcardPats extends AtomicFeature({
    case _ : scala.meta.Pat.Wildcard => true
  })
  case object AllowSeqwildcardPats extends AtomicFeature({
    case _ : scala.meta.Pat.SeqWildcard => true
  })
  case object AllowVarPats extends AtomicFeature({
    case _ : scala.meta.Pat.Var => true
  })
  case object AllowBindPats extends AtomicFeature({
    case _ : scala.meta.Pat.Bind => true
  })
  case object AllowAlternativePats extends AtomicFeature({
    case _ : scala.meta.Pat.Alternative => true
  })
  case object AllowTuplePats extends AtomicFeature({
    case _ : scala.meta.Pat.Tuple => true
  })
  case object AllowExtractPats extends AtomicFeature({
    case _ : scala.meta.Pat.Extract => true
  })
  case object AllowExtractinfixPats extends AtomicFeature({
    case _ : scala.meta.Pat.ExtractInfix => true
  })
  case object AllowInterpolatePats extends AtomicFeature({
    case _ : scala.meta.Pat.Interpolate => true
  })
  case object AllowXmlPats extends AtomicFeature({
    case _ : scala.meta.Pat.Xml => true
  })
  case object AllowTypedPats extends AtomicFeature({
    case _ : scala.meta.Pat.Typed => true
  })
  case object AllowCases extends AtomicFeature({
    case _ : scala.meta.Case => true
  })
  case object AllowTypecases extends AtomicFeature({
    case _ : scala.meta.TypeCase => true
  })
  case object AllowNullLits extends AtomicFeature({
    case _ : scala.meta.Lit.Null => true
  })
  case object AllowBooleanLits extends AtomicFeature({
    case _ : scala.meta.Lit.Boolean => true
  })
  case object AllowUnitLits extends AtomicFeature({
    case _ : scala.meta.Lit.Unit => true
  })
  case object AllowIntLits extends AtomicFeature({
    case _ : scala.meta.Lit.Int => true
  })
  case object AllowDoubleLits extends AtomicFeature({
    case _ : scala.meta.Lit.Double => true
  })
  case object AllowFloatLits extends AtomicFeature({
    case _ : scala.meta.Lit.Float => true
  })
  case object AllowLongLits extends AtomicFeature({
    case _ : scala.meta.Lit.Long => true
  })
  case object AllowByteLits extends AtomicFeature({
    case _ : scala.meta.Lit.Byte => true
  })
  case object AllowShortLits extends AtomicFeature({
    case _ : scala.meta.Lit.Short => true
  })
  case object AllowCharLits extends AtomicFeature({
    case _ : scala.meta.Lit.Char => true
  })
  case object AllowSymbolLits extends AtomicFeature({
    case _ : scala.meta.Lit.Symbol => true
  })
  case object AllowStringLits extends AtomicFeature({
    case _ : scala.meta.Lit.String => true
  })
  case object AllowNames extends AtomicFeature({
    case _ : scala.meta.Name => true
  })
  case object AllowSources extends AtomicFeature({
    case _ : scala.meta.Source => true
  })
  case object AllowPkgs extends AtomicFeature({
    case _ : scala.meta.Pkg => true
  })
  case object AllowObjectPkgs extends AtomicFeature({
    case _ : scala.meta.Pkg.Object => true
  })
  case object AllowTemplates extends AtomicFeature({
    case _ : scala.meta.Template => true
  })
  case object AllowSelfs extends AtomicFeature({
    case _ : scala.meta.Self => true
  })


}
