
class Foo(x: Int)
  def plus(fy: Foo): Foo = Foo(x + fy.x)
end Foo
