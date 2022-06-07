/*
mode = whitelist
allowedFeatures = [
  LiteralsAndExpressions,
  BasicOop,
  Defs
]
*/

class Foo(x: Int)
  def plus(fy: Foo): Foo = Foo(x + fy.x)
end Foo
