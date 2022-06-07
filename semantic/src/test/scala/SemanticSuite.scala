import semantic.Checker
import semantic.Violation
import semantic.Method
import semantic.Rules._

import scala.util.Properties
import tastyquery.jdk.ClasspathLoaders
import tastyquery.Contexts
import tastyquery.Contexts.BaseContext
import tastyquery.ast.Types._
import tastyquery.ast.Symbols._
import tastyquery.ast.Spans._
import tastyquery.ast.Names._

class SemanticSuite extends munit.FunSuite {

  val cpElem = Properties.propOrNone("test-resources").get
  val classpath =
    ClasspathLoaders.read(List(cpElem), Set(ClasspathLoaders.FileKind.Tasty))
  given BaseContext = Contexts.init(classpath)

  test("no-list-head") {
    val className = "res.ListHead"
    val checker = Checker(NoListHead)
    val violations = checker.checkClass(className)

    assertEquals(
      violations,
      List(
        Violation(
          Method(
            AppliedType(
              TypeRef(
                TermRef(PackageRef(SimpleName("scala")), SimpleName("package")),
                TypeName(SimpleName("List"))
              ),
              List(
                TypeRef(
                  PackageRef(SimpleName("scala")),
                  TypeName(SimpleName("Int"))
                )
              )
            ),
            SimpleName("head"),
            Span(72, 81, 77)
          ),
          Some("List#head is unsafe")
        )
      )
    )
  }

}
