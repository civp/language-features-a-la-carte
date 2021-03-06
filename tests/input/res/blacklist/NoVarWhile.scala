/*
mode = blacklist
rules = [
  NoVar,
  NoWhile
]
*/
abstract class NoVarWhile {
  private var w: Int = 0 /* violation: NoVar
  ^^^^^^^^^^^
  usage of var is forbidden */
  var y: Int /* violation: NoVar
  ^^^
  usage of var is forbidden */
  val z: String

  def f(x: Int, y: Int): Int = 2*x+y+x*x*x

  def foo(str: String, bar: Option[String]): String = {
    if (str.length > 5){
      bar match {
        case Some(value) => value ++ str ++ s"${null}"
        case None => if (str.length < 10) str else null
      }
    }
    else {
      var x = 0 /* violation: NoVar
      ^^^
      usage of var is forbidden */
      while (x < 1000){ /* violation: NoWhile
      ^^^^^
      usage of while and do-while loops is forbidden */
        x = x + (2*x-5) % 15
        y = 2
        while (y > -50){ /* violation: NoWhile
        ^^^^^
        usage of while and do-while loops is forbidden */
          y = y - 1
          val p = null
          y = y + p.asInstanceOf[Int]
        }
      }
      // do { /* violation: NoWhile
      // ^^
      // usage of while and do-while loops is forbidden */
      //   y = y + 5
      // } while (y < 0)
      y.toString
    }
  }

}
