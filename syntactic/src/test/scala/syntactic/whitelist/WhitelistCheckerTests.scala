package syntactic.whitelist

import org.junit.Test
import syntactic.whitelist.Features._
import syntactic.whitelist.WhitelistCheckerTestRunner.{expectInvalidWhenExcludingFeatures, expectParsingError, expectValidWithFeatures}

import scala.meta.dialects

class WhitelistCheckerTests {

  @Test def allowLiteralsAndExpressions_should_allow_literals(): Unit = {
    expectValidWithFeatures(
      srcFileName = "LiteralsOnly",
      features = List(LiteralsAndExpressions),
      dialect = dialects.Sbt1
    )
  }

  @Test def feature_LiteralsAndExpressions_should_allow_literals_and_expressions(): Unit = {
    expectValidWithFeatures(
      srcFileName = "LiteralsAndExpressions",
      features = List(LiteralsAndExpressions),
      dialect = dialects.Sbt1
    )
  }

  @Test def vals_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Vals",
      excludedFeatures = List(Vals),
      expectedViolationCnts = Map(5 -> 1, 6 -> 1, 2 -> 1, 3 -> 1, 4 -> 1)
    )
  }

  @Test def vals_should_be_accepted_when_allowed(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Vals",
      features = List(LiteralsAndExpressions, Vals)
    )
  }

  @Test def defs_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Defs",
      excludedFeatures = List(Defs),
      expectedViolationCnts = Map(2 -> 1, 3 -> 1)
    )
  }

  @Test def feature_ADTs_should_allow_ADTs(): Unit = {
    expectValidWithFeatures(
      srcFileName = "ADTs",
      features = List(LiteralsAndExpressions, ADTs)
    )
  }

  @Test def ADTs_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "ADTs",
      excludedFeatures = List(ADTs, BasicOop, AdvancedOop),
      expectedViolationCnts = Map(5 -> 3, 6 -> 3, 2 -> 2, 3 -> 1, 4 -> 1)
    )
  }

  @Test def feature_ADTs_should_not_allow_sealed_trait_with_non_case_class(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "NotSoADT",
      excludedFeatures = List(BasicOop, AdvancedOop),
      expectedViolationCnts = Map(2 -> 1, 3 -> 1)
    )
  }

  @Test def feature_BasicOop_should_allow_sealed_trait_with_non_case_class(): Unit = {
    expectValidWithFeatures(
      srcFileName = "NotSoADT",
      features = List(BasicOop)
    )
  }

  @Test def feature_LiteralFunctions_should_allow_literal_functions(): Unit = {
    expectValidWithFeatures(
      srcFileName = "LiteralFunctions",
      features = List(LiteralFunctions, LiteralsAndExpressions, Vals),
      dialect = dialects.Sbt1
    )
  }

  @Test def literal_functions_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "LiteralFunctions",
      excludedFeatures = List(LiteralFunctions),
      expectedViolationCnts = Map(5 -> 1, 6 -> 3, 2 -> 1, 7 -> 1, 3 -> 1, 4 -> 2),
      dialect = dialects.Sbt1
    )
  }

  @Test def feature_ForExpr_should_allow_for_expressions(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Fors",
      features = List(LiteralsAndExpressions, Defs, ForExpr)
    )
  }

  @Test def for_expressions_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Fors",
      excludedFeatures = List(ForExpr),
      expectedViolationCnts = Map(3 -> 2, 4 -> 2, 5 -> 2)
    )
  }

  @Test def feature_Imports_should_allow_imports(): Unit = {
    expectValidWithFeatures(
      srcFileName = "ImportOnly",
      features = List(Imports)
    )
  }

  @Test def imports_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "ImportOnly",
      excludedFeatures = List(Imports),
      expectedViolationCnts = Map(1 -> 1)
    )
  }

  @Test def feature_Packages_should_allow_packages(): Unit = {
    expectValidWithFeatures(
      srcFileName = "PackageOnly",
      features = List(Packages)
    )
  }

  @Test def feature_Null_should_allow_null(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Nulls",
      features = List(LiteralsAndExpressions, Vals, Nulls)
    )
  }

  @Test def nulls_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Nulls",
      excludedFeatures = List(Nulls),
      expectedViolationCnts = Map(1 -> 1, 2 -> 1, 4 -> 1)
    )
  }

  @Test def feature_PolymorphicTypes_should_allow_polymorphic_types(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Polymorphism",
      features = List(LiteralsAndExpressions, Defs, BasicOop, PolymorphicTypes)
    )
  }

  @Test def polymorphic_types_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Polymorphism",
      excludedFeatures = List(PolymorphicTypes),
      expectedViolationCnts = Map(2 -> 2, 4 -> 2, 7 -> 1, 11 -> 2, 13 -> 1)
    )
  }

  @Test def feature_Laziness_should_allow_lazy_vals_and_args(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Laziness",
      features = List(Vals, Defs, LiteralsAndExpressions, Laziness)
    )
  }

  @Test def laziness_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Laziness",
      excludedFeatures = List(Laziness),
      expectedViolationCnts = Map(2 -> 1, 3 -> 1)
    )
  }

  @Test def feature_AdvancedOop_should_allow_advanced_oop_constructs(): Unit = {
    expectValidWithFeatures(
      srcFileName = "AdvancedOOP",
      features = List(LiteralsAndExpressions, AdvancedOop, Defs)
    )
  }

  @Test def advanced_oop_constructs_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "AdvancedOOP",
      excludedFeatures = List(AdvancedOop),
      expectedViolationCnts = Map(2 -> 1, 6 -> 1, 10 -> 1)
    )
  }

  @Test def feature_ImperativeConstructs_should_allow_imperative_constructs(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Imperative",
      features = List(LiteralsAndExpressions, Defs, ImperativeConstructs),
      dialect = dialects.Sbt1
    )
  }

  @Test def imperative_constructs_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Imperative",
      excludedFeatures = List(ImperativeConstructs),
      expectedViolationCnts = Map(5 -> 1, 14 -> 1, 2 -> 1, 7 -> 1, 3 -> 1, 18 -> 1, 8 -> 1, 15 -> 1),
      dialect = dialects.Sbt1
    )
  }

  @Test def feature_ContextualConstructs_should_allow_contextual_constructs(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Contextual",
      features = List(LiteralsAndExpressions, Defs, ContextualConstructs, PolymorphicTypes)
    )
  }

  @Test def contextual_constructs_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Contextual",
      excludedFeatures = List(ContextualConstructs),
      expectedViolationCnts = Map(3 -> 1, 6 -> 1, 8 -> 1)
    )
  }

  @Test def feature_Extensions_should_allow_extensions(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Extensions",
      features = List(LiteralsAndExpressions, Defs, Extensions)
    )
  }

  @Test def extensions_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Extensions",
      excludedFeatures = List(Extensions),
      expectedViolationCnts = Map(2 -> 1)
    )
  }

  @Test def feature_Metaprogramming_should_allow_macros(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Metaprogramming",
      features = List(LiteralsAndExpressions, Defs, ContextualConstructs, PolymorphicTypes, Metaprogramming)
    )
  }

  @Test def metaprogramming_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Metaprogramming",
      excludedFeatures = List(Metaprogramming),
      expectedViolationCnts = Map(5 -> 3)
    )
  }

  @Test def feature_Exports_should_allow_exports(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Export",
      features = List(LiteralsAndExpressions, BasicOop, Defs, Exports)
    )
  }

  @Test def exports_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Export",
      excludedFeatures = List(Exports),
      expectedViolationCnts = Map(7 -> 1)
    )
  }

  @Test def feature_Xml_should_allow_xml(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Xml",
      features = List(Vals, Xml)
    )
  }

  @Test def xml_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Xml",
      excludedFeatures = List(Xml),
      expectedViolationCnts = Map(2 -> 1, 3 -> 1)
    )
  }

  @Test def feature_StringInterpolation_should_allow_string_interpolation(): Unit = {
    expectValidWithFeatures(
      srcFileName = "StringInterpolation",
      features = List(LiteralsAndExpressions, Vals, StringInterpolation)
    )
  }

  @Test def string_interpolation_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "StringInterpolation",
      excludedFeatures = List(StringInterpolation),
      expectedViolationCnts = Map(3 -> 1, 4 -> 1, 6 -> 3)
    )
  }

  @Test def feature_Annotations_should_allow_annotations(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Annotations",
      features = List(LiteralsAndExpressions, Defs, BasicOop, Annotations)
    )
  }

  @Test def annotations_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Annotations",
      excludedFeatures = List(Annotations),
      expectedViolationCnts = Map(5 -> 1, 2 -> 1, 7 -> 1, 11 -> 1, 8 -> 1)
    )
  }

  @Test def feature_Infixes_should_allow_infixes(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Infixes",
      features = List(Defs, LiteralsAndExpressions, Infixes)
    )
  }

  @Test def infixes_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Infixes",
      excludedFeatures = List(Infixes),
      expectedViolationCnts = Map(2 -> 1)
    )
  }

  @Test def feature_Inlines_should_allow_inlines(): Unit = {
    expectValidWithFeatures(
      srcFileName = "Inlines",
      features = List(Defs, LiteralsAndExpressions, Inlines)
    )
  }

  @Test def inlines_should_be_rejected_when_not_allowed(): Unit = {
    expectInvalidWhenExcludingFeatures(
      srcFileName = "Inlines",
      excludedFeatures = List(Inlines),
      expectedViolationCnts = Map(2 -> 1)
    )
  }

  @Test def checker_should_support_classes_without_braces(): Unit = {
    expectValidWithFeatures(
      srcFileName = "NoBraces",
      features = List(LiteralsAndExpressions, BasicOop, Defs)
    )
  }

  @Test def checker_correctly_reports_parsing_error(): Unit = {
    expectParsingError(srcFileName = "Erroneous")
  }

}
