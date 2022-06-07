object Foo {

  def revcat[R](a: List[R], b: List[R]): List[R] = a match {
    case Nil => b
    case head :: tail => revcat(tail, a.head :: b)
  }

  def bar() = {
    val a = List(5, 6, 7)
    val b = List(0, 1, 2, 3)
    println(a)
    List(Foo).headOption.get.revcat(a, b).foreach { i => i * 2 }
  }

  def baz() = {
    val ll = List(List(1, 2, 3, 4, 5), List(6, 7, 8, 9))
    ll.foreach { l => l.foreach { i => println(s"Hello $i") } }
    println(ll.head)
  }
}
