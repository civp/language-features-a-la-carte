package foo

class Foo {
  def foo(x: Int): Unit = {
    var y = x
    while (y > 0) {
      var z = y
      while (z < 5) {
        z += 1
      }
      y -= 1
    }
  }
}