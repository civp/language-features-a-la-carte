import Feature._
import scala.meta._

object Features {

  // TODO restrictions on modifiers

  case class AllowLiterals(allowNull: Boolean = false) extends Feature {
    override def check(tree: Tree): Boolean = tree match {
      case _ : Lit.Null => allowNull
      case _ : Lit.Boolean => true
      case _ : Lit.Unit => true
      case _ : Lit.Int => true
      case _ : Lit.Double => true
      case _ : Lit.Float => true
      case _ : Lit.Long => true
      case _ : Lit.Byte => true
      case _ : Lit.Short => true
      case _ : Lit.Char => true
      case _ : Lit.Symbol => true
      case _ : Lit.String => true
      case _ => false
    }
  }

  case object AllowExpressions extends AtomicFeature({
    case _ : Term.Select => true
    case _ : Term.ApplyUnary => true
    case _ : Term.Apply => true
    case _ : Term.ApplyInfix => true
    case _ : Term.If => true
    case _ : Term.Interpolate => true
  })

  case object AllowVals extends AtomicFeature({
    case _ : Decl.Val => true
    case _ : Defn.Val => true
    case _ : Term.Name => true
    case _ : Term.Assign => true
  })

  case object AllowDefs extends AtomicFeature({
    case _ : Decl.Def => true
    case _ : Defn.Def => true
    case _ : Term.Assign => true
    case _ : Term.Repeated => true
    case _ : Type.Repeated => true
  })

  case object AllowADTs extends AtomicFeature({
    case Defn.Class((modLs, name, paramLs, primaryCtor, template)) => modLs.contains(Mod.Case)
    case Defn.Object((modLs, name, template)) => modLs.contains(Mod.Case)
    case _ : Defn.Enum => true
    case _ : Defn.EnumCase => true
    case _ : Defn.RepeatedEnumCase => true
    case _ : Term.This => true
    case _ : Term.Tuple => true
    case _ : Type.Tuple => true
    case _ : Term.Match => true
    case _ : Term.PartialFunction => true
    case _ : Term.New => true
    case _ : Lit => true
    case _ : Pat.Wildcard => true
    case _ : Pat.SeqWildcard => true
    case _ : Pat.Var => true
    case _ : Pat.Bind => true
    case _ : Pat.Alternative => true
    case _ : Pat.Tuple => true
    case _ : Pat.Extract => true
    case _ : Pat.ExtractInfix => true
    case _ : Pat.Interpolate => true
    case _ : Pat.Typed => true
    case _ : Case => true
  })

  case object AllowLiteralFunctions extends AtomicFeature({
    case _ : Term.Function => true
    case _ : Type.Function => true
  })

  case object AllowForExpr extends AtomicFeature({
    case _ : Term.For => true
    case _ : Term.ForYield => true
  })

  case object AllowPolymorphicTypes extends AtomicFeature({
    case _ : Decl.Type => true
    case _ : Defn.Type => true
    case _ : Type.Param => true
  })

  case object AllowLaziness extends AtomicFeature({
    case _ : Type.ByName => true
  })

  case object AllowRecursiveCalls extends AtomicFeature({
    ??? // TODO
  })

  private case object BasicOopAddition extends AtomicFeature({
    case Defn.Class((modLs, name, paramLs, primaryCtor, template)) => {
      modLs.contains(Mod.Case) || modLs.contains(Mod.Sealed)
    }
    case Defn.Trait((modLs, name, paramLs, primaryCtor, template)) => {
      modLs.contains(Mod.Sealed)
    }
    case Defn.Object((modLs, name, template)) => {
      modLs.contains(Mod.Case)
    }
    case _ : Term.Super => true

  })

  private case object AdvancedOOPAddition extends AtomicFeature({
    case _ : Term.NewAnonymous => true
  })

  case object AllowBasicOop extends CompositeFeature(AllowADTs, BasicOopAddition)

  case object AllowAdvancedOop extends CompositeFeature(AllowBasicOop, AdvancedOOPAddition)

  case object AllowImperativeConstructs extends AtomicFeature({
    case _ : Decl.Var => true
    case _ : Defn.Var => true
    case _ : Term.Return => true
    case _ : Term.Throw => true
    case _ : Term.Try => true
    case _ : Term.TryWithHandler => true
    case _ : Term.While => true
    case _ : Term.Do => true
  })

  case object AllowContextualConstructs extends AtomicFeature({
    case _ : Defn.Given => true
    case _ : Defn.GivenAlias => true
    case _ : Term.ApplyUsing => true
    case _ : Term.ContextFunction => true
    case _ : Type.ContextFunction => true
  })

  /*
  Should always be allowed:
    Term.Ascribe
    Term.Param
    Term.Block
    Term.Placeholder
    Term.EndMarker
    Type.Placeholder
    Type.Bounds
    Type.Name

  Not implemented:
    Defn.Macro
    Defn.ExtensionGroup
    Term.Annotate
    Term.Eta
    Term.Xml
    Term.QuotedMacroExpr
    Term.QuotedMacroType
    Term.SplicedMacroExpr
    Term.PolyFunction
    Type.Singleton
    Type.Apply
    Type.ApplyInfix
    Type.With
    Type.And
    Type.Or
    Type.Refine
    Type.Existential
    Type.Annotate
    Type.Lambda
    Type.Method
    Term.ApplyType
    Type.Select
    Type.Project
    Type.Var
    Type.PolyFunction
    Type.Match
    Pat.Xml
    Term.Macro
    Term.Given (found in doc. but not found by Intellij)
    TypeCase
    name.Indeterminate
   */

}
