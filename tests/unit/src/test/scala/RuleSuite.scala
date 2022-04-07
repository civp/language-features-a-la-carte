import scala.meta._
import scala.meta.internal.io.FileIO
import scala.meta.io.AbsolutePath
import java.nio.charset.StandardCharsets

import org.junit.Test
import org.junit.Assert._
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

class RuleSuite extends FunSuite {
  
  test("Example test") {
    val path = AbsolutePath("tests/input/src/main/scala/example/Example.scala")
    val sourceCode = FileIO.slurp(path, StandardCharsets.UTF_8)
    import output.example.outputs
    for (output <- outputs) {
      println(output)
      val detectedViolations = output.checker.check(sourceCode).get
      assertEquals(output.expected, detectedViolations.map(violation => (violation.pos.startLine, violation.pos.startColumn)))
    }
  }
}
