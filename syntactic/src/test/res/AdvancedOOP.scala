
transparent trait Baz {
  def lmnop(y: Int): Int
}

open class Foo {

  def abc(x: Int): Boolean = x.isWhole

  final def wxyz(s: String): Int = s.length

}

class FooImpl extends Foo, Baz {
  override def abc(x: Int): Boolean = x.isNaN
  override def lmnop(y: Int): Int = 2*y
}

case class Foo2(i: Int)
