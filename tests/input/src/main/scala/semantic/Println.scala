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
  println is not functional */

  def prtls(xs: List[String]): Unit = xs.foreach(println) // has no span

}
