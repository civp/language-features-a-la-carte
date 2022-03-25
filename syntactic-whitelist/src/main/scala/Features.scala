import Feature._

import scala.meta._

object Features {

  // TODO restrictions on modifiers

  case object AllowLiteralsAndExpressions extends Feature {
    override def check(tree: Tree): Boolean = tree match {
      case _ : Lit.Boolean => true
      case _ : Lit.Byte => true
      case _ : Lit.Char => true
      case _ : Lit.Double => true
      case _ : Lit.Float => true
      case _ : Lit.Int => true
      case _ : Lit.Long => true
      case _ : Lit.Short => true
      case _ : Lit.String => true
      case _ : Lit.Symbol => true
      case _ : Lit.Unit => true
      case _ : Term.Apply => true
      case _ : Term.ApplyInfix => true
      case _ : Term.ApplyUnary => true
      case _ : Term.If => true
      case _ : Term.Name => true
      case _ : Term.Select => true
      case _ : Term.Tuple => true
      case _ : Type.Tuple => true
      case _ : ImportExportStat => true
      case _ : Type => true
      case _ : Term.Ascribe => true
      case _ : Term.Repeated => true
      case _ => false
    }
  }

  case object AllowNull extends AtomicFeature({
    case _ : Lit.Null => true
  })

  case object AllowVals extends AtomicFeature({
    case _ : Decl.Val => true
    case _ : Defn.Val => true
    case _ : Pat.Var => true
    case _ : Term.Name => true
    case _ : Term.Anonymous => true
  })

  case object AllowDefs extends AtomicFeature({
    case _ : Decl.Def => true
    case _ : Defn.Def => true
    case _ : Term.Name => true
    case _ : Term.Param => true
    case _ : Type.Repeated => true
  })

  case object AllowADTs extends AtomicFeature({
    case _ : Case => true
//    case _ : Ctor => true
    case _ : Ctor.Primary => true
    case Defn.Class((modifiersList, typeName, paramLs, primaryCtor, template)) => {
      modifiersList.exists {
        case Mod.Case() => true
        case Mod.Sealed() => true
      }
    }
    case _ : Defn.Enum => true
    case _ : Defn.EnumCase => true
    case _ : Defn.Object => true
    case _ : Defn.RepeatedEnumCase => true
    case Defn.Trait((modifiersLs, typeName, paramLs, primaryCtor, template)) => {
      modifiersLs.exists {
        case Mod.Sealed() => true
      }
    }
    case Mod.Abstract() => true
    case Mod.Case() => true
    case Mod.Contravariant() => true
    case Mod.Covariant() => true
    case Mod.Override() => true
    case Mod.Private(ref) => true
    case Mod.Sealed() => true
    case Mod.ValParam() => true   // TODO do we really want to allow that?
    case _ : Pat.Tuple => true
    case _ : Pat.Typed => true
    case _ : Pat.Var => true
    case _ : Pat.Wildcard => true
    case _ : Term.Match => true
    case _ : Term.Param => true
    case _ : Term.PartialFunction => true
    case _ : CaseTree => true
    case _ : Pat.Alternative => true
    case _ : Pat.Bind => true
    case _ : Pat.Extract => true
    case _ : Pat.ExtractInfix => true
    case _ : Pat.Repeated => true
    case _ : Pat.SeqWildcard => true
    case _ : TypeCase => true
  })

  case object AllowAnonymousFunctions extends AtomicFeature({
    case _ : Term.AnonymousFunction => true
    case _ : Term.Placeholder => true
    case _ : Type.Placeholder => true
  })

  case object AllowLiteralFunctions extends AtomicFeature({
    case _ : Term.ContextFunction => true
    case _ : Term.Function => true
    case _ : Type.ContextFunction => true
    case _ : Type.Function => true
    case _ : Term.Param => true
    case _ : Term.Eta => true
  })

  case object AllowForExpr extends AtomicFeature({
    case _ : Term.For => true
    case _ : Term.ForYield => true
    case _ : Enumerator => true
    case _ : Pat.Var => true
  })

  case object AllowPolymorphicTypes extends AtomicFeature({
    case _ : Decl.Type => true
    case _ : Defn.Type => true
    case Mod.Opaque() => true
    case _ : Term.PolyFunction => true
    case _ : Type.Apply => true
    case _ : Type.Bounds => true
    case _ : Type.Match => true
    case _ : Type.Param => true
  })

  case object AllowLaziness extends AtomicFeature({
    case _ : Type.ByName => true
  })

//  case object AllowRecursiveCalls extends AtomicFeature({
//    ??? // TODO
//  })

  private case object BasicOopAddition extends AtomicFeature({
    case _ : Ctor.Secondary => true
    case _ : Defn.Class => true
    case Mod.Protected(ref) => true
    case _ : Term.New => true
    case _ : Term.Super => true
    case _ : Term.This => true
    case _ : Type.With => true
    case _ : Defn.Trait => true
    case _ : Term.NewAnonymous => true
  })

  private case object AdvancedOOPAddition extends AtomicFeature({
    case Mod.Final() => true
    case Mod.Open() => true
    case Mod.Transparent() => true
    case Mod.Super() => true
  })

  case object AllowBasicOop extends CompositeFeature(AllowADTs, BasicOopAddition)

  case object AllowAdvancedOop extends CompositeFeature(AllowBasicOop, AdvancedOOPAddition)

  case object AllowImperativeConstructs extends AtomicFeature({
    case _ : Decl.Var => true
    case _ : Defn.Var => true
    case Mod.VarParam() => true
    case _ : Term.Assign => true
    case _ : Term.Do => true
    case _ : Term.Name => true
    case _ : Term.Return => true
    case _ : Term.Throw => true
    case _ : Term.Try => true
    case _ : Term.TryWithHandler => true
    case _ : Term.While => true
  })

  case object AllowContextualConstructs extends AtomicFeature({
    case _ : Decl.Given => true
    case _ : Defn.Given => true
    case _ : Defn.GivenAlias => true
    case _ : Importee.Given => true     // TODO here or in import?
    case _ : Importee.GivenAll => true  // TODO here or in import?
    case Mod.Implicit() => true
    case Mod.Using() => true
    case _ : Pat.Given => true
    case _ : Type.ImplicitFunction => true  // TODO depreacted, keep it? Yes!
    case _ : Term.ApplyUsing => true
  })

  case object AllowExtensions extends AtomicFeature({
    case _ : Defn.ExtensionGroup => true

  })

  case object AllowMetaprogramming extends AtomicFeature({
    case _ : Defn.Macro => true
    case _ : Term.QuotedMacroExpr => true
    case _ : Term.QuotedMacroType => true
    case _ : Term.SplicedMacroExpr => true
    case _ : Term.SplicedMacroPat => true
    case _ : Type.Macro => true
    case _ : Pat.Macro => true
    case _ : internal.trees.Quasi => true
  })

  case object AllowImports extends AtomicFeature({
    case _ : Import => true
    case _ : Importee => true
    case _ : Importer => true

  })

  case object AllowExports extends AtomicFeature({
    case _ : Export => true
  })

  case object AllowPackages extends AtomicFeature({
    case _ : Pkg => true
    case _ : Pkg.Object => true
  })

  case object AllowXml extends AtomicFeature({
    case _ : Term.Xml => true
    case _ : Pat.Xml => true
  })

  case object AllowStringInterpolation extends AtomicFeature({
    case _ : Term.Interpolate => true
    case _ : Pat.Interpolate => true
  })

  case object AllowAnnotations extends AtomicFeature({
    case _ : Term.Annotate => true
    // TODO classes, traits, defs, vals with restrictions on modifiers
  })

  /*
  Not implemented  TODO figure out what they are
  Member.Term
  Member.Type
  MultiSource
  Name.Indeterminate
  Term.ApplyType
  Term.FunctionTerm
  Term.Ref
   */

}
