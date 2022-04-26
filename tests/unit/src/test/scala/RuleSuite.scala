import testkit.RuleTest
import testkit.TestPath

import scala.meta.io.AbsolutePath

class RuleSuite extends munit.FunSuite {

  private def getTestPath(name: String): TestPath = {
    val path = AbsolutePath(s"${System.getProperty("tests-input")}/$name.scala")
    new TestPath(name, path)
  }
  
  test("no-null-no-cast") {
    val path = getTestPath("example/NoNullNoCast")
    // Do something here
    assert(RuleTest.fromPath(path).run())
  }
}
