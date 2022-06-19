package syntactic.whitelist

import syntactic.whitelist.Feature.{AtomicFeature, CompositeFeature}

import scala.meta._

object PredefFeatures extends FeaturesProvider {

  /**
   * Allows literals of basic types, including Unit and tuples, as well as expressions
   */
  case object LiteralsAndExpressions extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Lit.Boolean => true
      case _: Lit.Byte => true
      case _: Lit.Char => true
      case _: Lit.Double => true
      case _: Lit.Float => true
      case _: Lit.Int => true
      case _: Lit.Long => true
      case _: Lit.Short => true
      case _: Lit.String => true
      case _: Lit.Symbol => true
      case _: Lit.Unit => true
      case _: Term.Apply => true
      case _: Term.ApplyInfix => true
      case _: Term.ApplyUnary => true
      case _: Term.If => true
      case _: Term.Tuple => true
      case _: Type.Tuple => true
      case _: Term.Ascribe => true
      case _: Term.Repeated => true
    }
  }

  /**
   * Allows the use of the null literal
   */
  case object Nulls extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Lit.Null => true
    }
  }

  /**
   * Allows the use of vals
   */
  case object Vals extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Decl.Val => true
      case _: Defn.Val => true
      case _: Term.Anonymous => true
    }
  }

  /**
   * Allows the use of defs
   */
  case object Defs extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Decl.Def => true
      case _: Defn.Def => true
      case _: Term.Param => true
      case _: Type.Repeated => true
    }
  }

  /**
   * Allows algebraic data types, i.e.:
   * case classes, sealed classes
   * sealed traits, enums
   * Also allows objects (and not only case objects) for convenience (e.g. companion objects)
   */
  case object ADTs extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
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
      case _: Defn.Object => true
      case Mod.Abstract() => true
      case Mod.Case() => true
      case Mod.Override() => true
      case Mod.Sealed() => true
      case Mod.ValParam() => true
      case _: Case => true
      case _: Ctor.Primary => true
      case _: Defn.Enum => true
      case _: Defn.EnumCase => true
      case _: Defn.RepeatedEnumCase => true
      case _: Pat.Tuple => true
      case _: Pat.Typed => true
      case _: Pat.Wildcard => true
      case _: Term.Match => true
      case _: Term.Param => true
      case _: Term.PartialFunction => true
      case _: CaseTree => true
      case _: Pat.Alternative => true
      case _: Pat.Bind => true
      case _: Pat.Extract => true
      case _: Pat.ExtractInfix => true
      case _: Pat.Repeated => true
      case _: Pat.SeqWildcard => true
    }
  }

  /**
   * Allows the use of literal functions
   */
  case object LiteralFunctions extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Term.ContextFunction => true
      case _: Term.Function => true
      case _: Type.ContextFunction => true
      case _: Type.Function => true
      case _: Term.Param => true
      case _: Term.Eta => true
      case _: Term.AnonymousFunction => true
      case _: Term.Placeholder => true
      case _: Type.Placeholder => true
    }
  }

  /**
   * Allows fors and for-yields
   */
  case object ForExpr extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Term.For => true
      case _: Term.ForYield => true
      case _: Enumerator => true
    }
  }

  /**
   * Allows polymorphism, including opaque types
   */
  case object PolymorphicTypes extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Opaque() => true
      case _: Decl.Type => true
      case _: Defn.Type => true
      case _: Term.PolyFunction => true
      case _: Type.Apply => true
      case _: Type.Bounds => true
      case _: Type.Match => true
      case _: Type.Param => true
      case _: Type.Function => true
      case _: Term.ApplyType => true
    }
  }

  /**
   * Allows lazy vals and by-name arguments
   */
  case object Laziness extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Lazy() => true
      case _: Type.ByName => true
    }
  }

  private case object BasicOopAddition extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Private(_) => true
      case Mod.Protected(_) => true
      case _: Ctor.Secondary => true
      case _: Defn.Class => true
      case _: Term.New => true
      case _: Term.Super => true
      case _: Term.This => true
      case _: Type.With => true
      case _: Defn.Trait => true
      case _: Term.NewAnonymous => true
    }
  }

  private case object AdvancedOOPAddition extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Final() => true
      case Mod.Open() => true
      case Mod.Transparent() => true
      case Mod.Super() => true
      case Mod.Contravariant() => true
      case Mod.Covariant() => true
    }
  }

  /**
   * In addition to constructs related to algebraic data types,
   * this allows basic object-oriented programming, including:
   * classes, traits, secondary constructors, access modifiers
   */
  case object BasicOop extends CompositeFeature(ADTs, BasicOopAddition)

  /**
   * In addition to constructs related to algebraic data types and basic object-oriented programming,
   * this allows advanced object-oriented modifiers:
   * final, open, transparent, super, covariant, contravariant, transparent
   */
  case object AdvancedOop extends CompositeFeature(BasicOop, AdvancedOOPAddition)

  /**
   * Allows the use of imperative constructs, including:
   * vars, try-throw-catches, return and while loops
   */
  case object ImperativeConstructs extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.VarParam() => true
      case _: Decl.Var => true
      case _: Defn.Var => true
      case _: Term.Assign => true
      case _: Term.Do => true
      case _: Term.Return => true
      case _: Term.Throw => true
      case _: Term.Try => true
      case _: Term.TryWithHandler => true
      case _: Term.While => true
    }
  }

  /**
   * Allows the use of contextual constructs, including:
   * implicits, using, givens
   */
  case object ContextualConstructs extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Implicit() => true
      case Mod.Using() => true
      case _: Decl.Given => true
      case _: Defn.Given => true
      case _: Defn.GivenAlias => true
      case _: Importee.Given => true
      case _: Importee.GivenAll => true
      case _: Pat.Given => true
      case _: Type.ImplicitFunction => true
      case _: Term.ApplyUsing => true
    }
  }

  /**
   * Allows the definition of extension groups and extension methods
   */
  case object Extensions extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Defn.ExtensionGroup => true
    }
  }

  /**
   * Allows the use of macro and metaprogramming-related constructs
   */
  case object Metaprogramming extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Defn.Macro => true
      case _: Term.QuotedMacroExpr => true
      case _: Term.QuotedMacroType => true
      case _: Term.SplicedMacroExpr => true
      case _: Term.SplicedMacroPat => true
      case _: Type.Macro => true
      case _: Pat.Macro => true
      case _: internal.trees.Quasi => true
    }
  }

  /**
   * Allows the use of packages
   */
  case object Packages extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Pkg => true
      case _: Pkg.Object => true
    }
  }

  /**
   * Allows the use of imports
   */
  case object Imports extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Import => true
      case _: Importee => true
      case _: Importer => true
      case _: Name.Indeterminate => true
    }
  }

  /**
   * Allows export clauses
   */
  case object Exports extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Export => true
      case _: Importee => true
      case _: Importer => true
      case _: Name.Indeterminate => true
    }
  }

  /**
   * Allows the use of XML-related constructs
   */
  case object Xml extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Term.Xml => true
      case _: Pat.Xml => true
      case _: Lit.String => true
    }
  }

  /**
   * Allows string interpolation
   */
  case object StringInterpolation extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Term.Interpolate => true
      case _: Pat.Interpolate => true
    }
  }

  /**
   * Allows annotations
   */
  case object Annotations extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case _: Term.Annotate => true
      case _: Mod.Annot => true
    }
  }

  /**
   * Allows to define infix methods
   */
  case object Infixes extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Infix() => true
    }
  }

  /**
   * Allows to define inline methods
   */
  case object Inlines extends AtomicFeature {
    override val checkPF: PartialFunction[Tree, Boolean] = {
      case Mod.Inline() => true
    }
  }

}
