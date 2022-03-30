/*
noNull = true
noCast = true
*/
abstract class NoNullNoCast {
  private var w: Int = 0
  var y: Int
  val z: String

  def f(x: Int, y: Int): Int = 2*x+y+x*x*x

  def foo(str: String, bar: Option[String]): String = {
    if (str.length > 5){
      bar match {
        case Some(value) => value ++ str ++ s"${null}" /* assert: noNull
                                                ^^^^
        null is blacklisted */
        case None => if (str.length < 10) str else null /* assert: noNull
                                                   ^^^^
        null is blacklisted */
      }
    }
    else {
      var x = 0
      while (x < 1000){
        x = x + (2*x-5) % 15
        y = 2
        while (y > -50){
          y = y - 1
          val p = null /* assert: noNull
                  ^^^^
          null is blacklisted */
          y = y + p.asInstanceOf[Int] /* assert: noNull
                    ^^^^^^^^^^^^
          cast is disabled */
        }
      }
      do {
        y = y + 5
      } while (y < 0)
      y.toString
    }
  }

}
