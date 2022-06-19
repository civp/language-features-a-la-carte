/*
mode = blacklist
rules = [
  NoIterableOnceOpsForeach
]
*/
package res

class Foreach {

  def printList(xs: List[Int]): Unit =
    xs.foreach(println) /*
    ^^^^^^^^^^^^^^^^^^^
    foreach performs side-effects */

  def printMatrix(mtx: List[List[Int]]): Unit =
    mtx.foreach(_.foreach(println)) /*
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    foreach performs side-effects */

}
