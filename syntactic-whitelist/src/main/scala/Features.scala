import Feature._

import scala.meta.Pat.Given
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
      case _ : Lit.Symbol => true  // FIXME deprecated, keep it?
      case _ : Lit.Unit => true
      case _ : Term.Apply => true
      case _ : Term.ApplyInfix => true
      case _ : Term.ApplyUnary => true
    }
  }

  case object AllowNull extends AtomicFeature({
    case _ : Lit.Null => true
  })

  case object AllowVals extends AtomicFeature({
    case _ : Decl.Val => true
    case _ : Defn.Val => true
    case _ : Pat.Var => true
  })

  case object AllowDefs extends AtomicFeature({
    case _ : Decl.Def => true
    case _ : Defn.Def => true

  })

  case object AllowADTs extends AtomicFeature({
    case _ : Case => true
    case _ : Ctor => true
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

  })

  case object AllowLiteralFunctions extends AtomicFeature({

  })

  case object AllowForExpr extends AtomicFeature({

  })

  case object AllowPolymorphicTypes extends AtomicFeature({
    case _ : Decl.Type => true
    case _ : Defn.Type => true
    case Mod.Opaque() => true
  })

  case object AllowLaziness extends AtomicFeature({

  })

  case object AllowRecursiveCalls extends AtomicFeature({

  })

  private case object BasicOopAddition extends AtomicFeature({
    case _ : Ctor.Secondary => true
    case _ : Defn.Class => true
    case Mod.Protected(ref) => true
  })

  private case object AdvancedOOPAddition extends AtomicFeature({
    case Mod.Final() => true
    case Mod.Open() => true
    case Mod.Transparent() => true
  })

  case object AllowBasicOop extends CompositeFeature(AllowADTs, BasicOopAddition)

  case object AllowAdvancedOop extends CompositeFeature(AllowBasicOop, AdvancedOOPAddition)

  case object AllowImperativeConstructs extends AtomicFeature({
    case _ : Decl.Var => true
    case _ : Defn.Var => true
    case Mod.VarParam() => true
    case _ : Term.Assign => true
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
  })

  case object AllowExtensions extends AtomicFeature({
    case _ : Defn.ExtensionGroup => true

  })

  case object AllowMacros extends AtomicFeature({
    case _ : Defn.Macro => true

  })

  case object AllowImports extends AtomicFeature({
    case _ : Import => true
    case _ : Importee => true
    case _ : Importee.Name => true
    case _ : Importee.Rename => true
    case _ : Importee.Unimport => true
    case _ : Importee.Wildcard => true
    case _ : Importer => true

  })

  case object AllowExports extends AtomicFeature({
    case _ : Export => true
  })

  case object AllowPackages extends AtomicFeature({
    case _ : Pkg => true
    case _ : Pkg.Object => true
  })

  /*
  Not implemented  TODO figure out what they are
  CaseTree
  Decl
  Defn
  Enumerator
  Enumerator.CaseGenerator
  Enumerator.Generator
  Enumerator.Guard
  Enumerator.Val
  ImportExportStat
  Init
  Lit
  Member
  Member.Term
  Member.Type
  Mod
  Mod.Annot
  Mod.Infix
  Mod.Inline
  Mod.Lazy
  MultiSource
  Name
  Name.Anonymous
  Name.Indeterminate
  Pat
  Pat.Alternative
  Pat.Bind
  Pat.Extract
  Pat.ExtractInfix
  Pat.Interpolate
  Pat.Macro
  Pat.Repeated
  Pat.SeqWildcard
  Pat.Xml
  Ref
  Self
  Stat
  Term
  Term.Annotate
  Term.Anonymous
  Term.AnonymousFunction
  Term.ApplyType
  Term.ApplyUsing
  Term.Ascribe

   */

}
