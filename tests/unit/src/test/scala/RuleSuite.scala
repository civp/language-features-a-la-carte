import scala.meta._
import scala.meta.internal.io.FileIO
import scala.meta.io.AbsolutePath
import java.nio.charset.StandardCharsets
import org.junit.Test
import org.junit.Assert._
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import syntactic_checker.CheckResult

class RuleSuite extends FunSuite {
  
  test("Example test") {
    val path = AbsolutePath("tests/input/src/main/scala/example/Example.scala")
    val sourceCode = FileIO.slurp(path, StandardCharsets.UTF_8)
    import output.example.outputs
    for (output <- outputs) {
      println(output)
      val checkResult = output.checker.checkCodeString(dialect = dialects.Scala213, sourceCode)
      checkResult match {
        case CheckResult.Valid => fail("should not be valid")
        case CheckResult.Invalid(detectedViolations) => {
          assertEquals(output.expected, detectedViolations.map(violation => (violation.pos.startLine, violation.pos.startColumn)))
        }
        case CheckResult.ParsingError(cause) => throw cause
      }
    }
  }
}
