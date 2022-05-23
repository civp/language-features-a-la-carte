/*
mode = blacklist
rules = [
  NoVar,
  NoWhile
]
*/
abstract class NoVarNoWhile {
  private var w: Int = 0 /* assert: noVar
          ^^^
  var is blacklisted */
  var y: Int /* assert: noVar
  ^^^
  var is blacklisted */
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
      var x = 0 /* assert: noVar
      ^^^
      var is diabled */
      while (x < 1000){ /* assert: noWhile
      ^^^^^
      while is disabled */
        x = x + (2*x-5) % 15
        y = 2
        while (y > -50){ /* assert: noWhile
        ^^^^^
        while is disabled */
          y = y - 1
          val p = null
          y = y + p.asInstanceOf[Int]
        }
      }
      do { /* assert: noWhile
      ^^
      while is disabled */
        y = y + 5
      } while (y < 0)
      y.toString
    }
  }

}
