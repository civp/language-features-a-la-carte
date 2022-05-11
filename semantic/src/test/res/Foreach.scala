package res

class Foreach {

  def printList(xs: List[Int]): Unit =
    xs.foreach(println)

  def printMatrix(mtx: List[List[Int]]): Unit =
    mtx.foreach(_.foreach(println))

}
