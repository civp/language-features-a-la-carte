import Feature._

import scala.meta._

object Features {

  /**
   * Allows literals of basic types, including Unit and tuples, as well as expressions
   */
  case object AllowLiteralsAndExpressions extends AtomicFeature({
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
  })

  /**
   * Allows the use of the null literal
   */
  case object AllowNull extends AtomicFeature({
    case _ : Lit.Null => true
  })

  /**
   * Allows the use of vals
   */
  case object AllowVals extends AtomicFeature({
    case _ : Decl.Val => true
    case _ : Defn.Val => true
    case _ : Pat.Var => true
    case _ : Term.Name => true
    case _ : Term.Anonymous => true
  })

  /**
   * Allows the use of defs
   */
  case object AllowDefs extends AtomicFeature({
    case _ : Decl.Def => true
    case _ : Defn.Def => true
    case _ : Term.Name => true
    case _ : Term.Param => true
    case _ : Type.Repeated => true
  })

  // TODO one more feature for objects only (to create modules)?

  /**
   * Allows algebraic data types, i.e.:
   * case classes, sealed classes
   * sealed traits, enums
   * Also allows objects (and not only case objects) for convenience (e.g. companion objects)
   */
  case object AllowADTs extends AtomicFeature({
    case Defn.Class(modifiers, _, _, _, _) => modifiers.exists {
      case Mod.Case() => true
      case Mod.Sealed() => true
      case _ => false
    }
    case Defn.Trait(modifiers, _, _, _, _) =>
      modifiers.exists {
        case Mod.Sealed() => true
        case _ => false
      }
    case _ : Defn.Object => true
    case Mod.Abstract() => true
    case Mod.Case() => true
    case Mod.Override() => true
    case Mod.Sealed() => true
    case Mod.ValParam() => true
    case _ : Case => true
    case _ : Ctor.Primary => true
    case _ : Defn.Enum => true
    case _ : Defn.EnumCase => true
    case _ : Defn.RepeatedEnumCase => true
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

  /**
   * Allows the use of anonymous functions
   */
  case object AllowAnonymousFunctions extends AtomicFeature({
    case _ : Term.AnonymousFunction => true
    case _ : Term.Placeholder => true
    case _ : Type.Placeholder => true
  })

  /**
   * Allows the use of literal functions
   */
  case object AllowLiteralFunctions extends AtomicFeature({
    case _ : Term.ContextFunction => true
    case _ : Term.Function => true
    case _ : Type.ContextFunction => true
    case _ : Type.Function => true
    case _ : Term.Param => true
    case _ : Term.Eta => true
  })

  /**
   * Allows fors and for-yields
   */
  case object AllowForExpr extends AtomicFeature({
    case _ : Term.For => true
    case _ : Term.ForYield => true
    case _ : Enumerator => true
    case _ : Pat.Var => true
  })

  /**
   * Allows polymorphism, including opaque types
   */
  case object AllowPolymorphicTypes extends AtomicFeature({
    case Mod.Opaque() => true
    case _ : Decl.Type => true
    case _ : Defn.Type => true
    case _ : Term.PolyFunction => true
    case _ : Type.Apply => true
    case _ : Type.Bounds => true
    case _ : Type.Match => true
    case _ : Type.Param => true
  })

  /**
   * Allows lazy vals and by-name arguments
   */
  case object AllowLaziness extends AtomicFeature({
    case Mod.Lazy() => true
    case _ : Type.ByName => true
  })

//  case object AllowRecursiveCalls extends AtomicFeature({
//    ???
//  })

  private case object BasicOopAddition extends AtomicFeature({
    case Mod.Private(_) => true
    case Mod.Protected(_) => true
    case _ : Ctor.Secondary => true
    case _ : Defn.Class => true
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
    case Mod.Contravariant() => true
    case Mod.Covariant() => true
    case Mod.Transparent() => true
  })

  /**
   * In addition to constructs related to algebraic data types,
   * this allows basic object-oriented programming, including:
   * classes, traits, secondary constructors, access modifiers
   */
  case object AllowBasicOop extends CompositeFeature(AllowADTs, BasicOopAddition)

  /**
   * In addition to constructs related to algebraic data types and basic object-oriented programming,
   * this allows advanced object-oriented modifiers:
   * final, open, transparent, super, covariant, contravariant, transparent
   */
  case object AllowAdvancedOop extends CompositeFeature(AllowBasicOop, AdvancedOOPAddition)

  /**
   * Allows the use of imperative constructs, including:
   * vars, try-throw-catches, return and while loops
   */
  case object AllowImperativeConstructs extends AtomicFeature({
    case Mod.VarParam() => true
    case _ : Decl.Var => true
    case _ : Defn.Var => true
    case _ : Term.Assign => true
    case _ : Term.Do => true
    case _ : Term.Name => true
    case _ : Term.Return => true
    case _ : Term.Throw => true
    case _ : Term.Try => true
    case _ : Term.TryWithHandler => true
    case _ : Term.While => true
  })

  /**
   * Allows the use of contextual constructs, including:
   * implicits, using, givens
   */
  case object AllowContextualConstructs extends AtomicFeature({
    case Mod.Implicit() => true
    case Mod.Using() => true
    case _ : Decl.Given => true
    case _ : Defn.Given => true
    case _ : Defn.GivenAlias => true
    case _ : Importee.Given => true
    case _ : Importee.GivenAll => true
    case _ : Pat.Given => true
    case _ : Type.ImplicitFunction => true
    case _ : Term.ApplyUsing => true
  })

  /**
   * Allows the definition of extension groups and extension methods
   */
  case object AllowExtensions extends AtomicFeature({
    case _ : Defn.ExtensionGroup => true
  })

  /**
   * Allows the use of macro and metaprogramming-related constructs
   */
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

  /**
   * Allows the use of imports
   */
  case object AllowImports extends AtomicFeature({
    case _ : Import => true
    case _ : Importee => true
    case _ : Importer => true
  })

  /**
   * Allows export clauses
   */
  case object AllowExports extends AtomicFeature({
    case _ : Export => true
  })

  /**
   * Allows the use of packages
   */
  case object AllowPackages extends AtomicFeature({
    case _ : Pkg => true
    case _ : Pkg.Object => true
  })

  /**
   * Allows the use of XML-related constructs
   */
  case object AllowXml extends AtomicFeature({
    case _ : Term.Xml => true
    case _ : Pat.Xml => true
  })

  /**
   * Allows string interpolation
   */
  case object AllowStringInterpolation extends AtomicFeature({
    case _ : Term.Interpolate => true
    case _ : Pat.Interpolate => true
  })

  /**
   * Allows annotations
   */
  case object AllowAnnotations extends AtomicFeature({
    case _ : Term.Annotate => true
    case _ : Mod.Annot => true
  })

  /**
   * Allows to define infix methods
   */
  case object AllowInfixes extends AtomicFeature({
    case Mod.Infix() => true
  })

  /**
   * Allows to define inline methods
   */
  case object AllowInlines extends AtomicFeature({
    case Mod.Inline() => true
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
