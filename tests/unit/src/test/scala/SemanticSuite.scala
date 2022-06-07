import semantic.Checker
import semantic.Violation
import semantic.Method
import semantic.Rules._

import scala.util.Properties
import tastyquery.jdk.ClasspathLoaders
import tastyquery.Contexts
import tastyquery.Contexts.BaseContext
import tastyquery.ast.Trees._
import tastyquery.ast.Types._
import tastyquery.ast.Symbols._
import tastyquery.ast.Spans._
import tastyquery.ast.Names._

class SemanticSuite extends munit.FunSuite {

  private val cpElem = Properties.propOrNone("tests-input-product").get
  private val classpath =
    ClasspathLoaders.read(List(cpElem), Set(ClasspathLoaders.FileKind.Tasty))
  given BaseContext = Contexts.init(classpath)

  type MethodCheck = PartialFunction[Method, Unit]
  
  private def isDefinedAt(p: MethodCheck, methods: List[Method]): Boolean =
    // p is defined at each method
    methods.foldLeft(true)((b, m) => b && p.isDefinedAt(m))

  test("no-list-head") {
    val className = "res.ListHead"
    val checker = Checker(NoListHead)
    val violations = checker.checkClass(className)
    val methods = violations.map(_.method)
    val listHead: MethodCheck = {
      case Method(
        AppliedType(TypeRef(_, TypeName(SimpleName("List"))), _),
        SimpleName("head"),
        _
      ) =>
    }
    // TODO: complete the checks
    assertEquals(methods.size, 2)
    assert(isDefinedAt(listHead, methods))
  }

  test("no-option-get") {
    val className = "res.OptionGet"
    val checker = Checker(NoOptionGet)
    val violations = checker.checkClass(className)
    val methods = violations.map(_.method)
    val optionGet: MethodCheck = {
      case Method(
        AppliedType(TypeRef(_, TypeName(SimpleName("Option"))), _),
        SimpleName("get"),
        _
      ) =>
    }
    // TODO: complete the checks
    assertEquals(methods.size, 1)
    assert(isDefinedAt(optionGet, methods))
  }

  test("no-println") {
    val className = "res.Println"
    val checker = Checker(NoPrintln)
    val violations = checker.checkClass(className)
    val methods = violations.map(_.method)
    val printl: MethodCheck = {
      case Method(
        NoType,
        SimpleName("println"),
        _,
      ) =>
    }
    // TODO: complete the checks
    assertEquals(methods.size, 2)
    assert(isDefinedAt(printl, methods))
  }

  test("my-println") {
    val className = "res.MyPrintln"
    val checker = Checker(NoPrintln)
    val violations = checker.checkClass(className)
    val methods = violations.map(_.method)
    assertEquals(methods, Nil)
  }

  test("no-foreach") {
    val className = "res.Foreach"
    val checker = Checker(NoIterableOnceOpsForeach)
    val violations = checker.checkClass(className)
    val methods = violations.map(_.method)
    val foreach: MethodCheck = {
      case Method(
        _, // TODO: subtype
        SimpleName("foreach"),
        _
      ) =>
    }
    // TODO: complete the checks
    assertEquals(methods.size, 3)
    assert(isDefinedAt(foreach, methods))
  }

}
