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
    foreach is not functional */

  def printMatrix(mtx: List[List[Int]]): Unit =
    mtx.foreach(_.foreach(println)) /*
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    foreach is not functional */

}
