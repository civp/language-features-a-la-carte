
def foo(): Unit = {
  for (i <- 0 until 100){
    for (j <- 0 until 50){
      for (k <- 0 until 80){
        ()
      }
    }
  }
}
