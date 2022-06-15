import translator.{Reporter, Translator}

import java.io.{File, PrintWriter}
import scala.meta.{Source, dialects}
import scala.util.Using


object TransformationDemo {

  def main(args: Array[String]): Unit = {

    val path = "demo-valentin/src/main/examples/Example.scala"
    val srcCode = Using(scala.io.Source.fromFile(path)){ bufSource =>
      bufSource.getLines().mkString("\n")
    }.get

    val srcTree = dialects.Scala213(srcCode).parse[Source].get

    val reporter = new Reporter()
    val translator = Translator(reporter)
    val translatedTree = translator.translateMethodsIn(srcTree)
    reporter.getReportedErrors.foreach(println)

    Using(new PrintWriter(new File(path))){ writer =>
      writer.write(translatedTree.syntax)
    }

  }

}


