/*
mode = blacklist
rules = [
  NoPrintln
]
*/

package res

class Println {
  
  def prtl(x: String): Unit = println(x) /*
                              ^^^^^^^^^^
  println performs side-effects */

  def prtls(xs: List[String]): Unit = xs.foreach(println) // has no span

}
