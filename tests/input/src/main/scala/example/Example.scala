
abstract class Example {
  private var w: Int = 0
  var y: Int
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
      var x = 0
      while (x < 1000){
        x = x + (2*x-5) % 15
        y = 2
        while (y > -50){
          y = y - 1
          val p = null
          y = y + p.asInstanceOf[Int]
        }
      }
      do {
        y = y + 5
      } while (y < 0)
      y.toString
    }
  }

}
