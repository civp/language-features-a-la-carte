
var x = 0
while (x < 1000){
  println(x)
  x = x + 1
}
var y = 1
do {
  println(y)
  y += 2
} while(y < 500)

def foo(y: Int): Int = {
  if (y < 0) return 0
  var k = 2*y
  if (k / 5 == k * 5){
    println("Scala!")
    return -42
  }
  if (k % 2 == 0) k else -1
}