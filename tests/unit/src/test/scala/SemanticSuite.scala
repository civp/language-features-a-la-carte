import org.scalatest.FunSuite
import semantic.Checker

class SemanticSuite extends FunSuite {
  
  test("Safer language checker") {
    val path = "tests/input/src/main/scala/semantic/Foo.tasty"
    // import output.example.outputs
    val checker = new Checker(Nil)
    println(checker.check(path))
    // val detectedViolations = output.checker.check(sourceCode).get
    // assertEquals(output.expected, detectedViolations.map(violation => (violation.pos.startLine, violation.pos.startColumn)))
  }
}
