package foo.baz

class Baz {
  def baz: Unit = {
    var x = 42
    while (x > 0) do x -= 1
  }
}