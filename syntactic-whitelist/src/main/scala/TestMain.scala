import Features._

import scala.meta._

object TestMain {

  def main(args: Array[String]): Unit = {
    val checker = Checker(
      AllowAllEssentialFeatures, AllowObjectDefns, AllowAllDefDefinitionFeatures, AllowApplyTypes, AllowApplyTerms,
      AllowBlockTerms, AllowStringLits
    )
    val emptyTemplate = Template(Nil, Nil, Self(Name(""), None), Nil)
    val src =
      """
        |object Main {
        |  def main(args: Array[String]): Unit = {
        |    println("Hello world")
        |  }
        |}
        |""".stripMargin.parse[Source].get
    src.traverse(t => {
      val check = checker.check(t)
      if (!check){
        println(s"${t.getClass}[REJECTED]: $t\n-----------------------\n")
      }
    })
    print("\nGLOBALLY: ")
    println(checker.check(src))
  }

}
