
class Foo[T]

trait Baz[U] {
  def abcd(): U
}
case class Bar(x: Int) extends Baz[Int]{
  def abcd(): Int = 0
}

def wxyz[SomeType](st: SomeType): SomeType = st

def f() = wxyz[Int](0)
