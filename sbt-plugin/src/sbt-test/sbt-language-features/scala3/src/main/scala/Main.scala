@main def foo() =
  var x: Int | Null = 3
  if x == null then
    x = 0
  else
    while x > 0 do x -= 1