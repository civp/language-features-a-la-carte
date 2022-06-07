package res

class Println {
  
  def prtl(x: String): Unit = println(x)

  def prtls(xs: List[String]): Unit = xs.foreach(println)

}

class MyPrintln {

  def println(x: String): Unit = print(x + "\n")

  def prtl(x: String): Unit = println(x)

  def prtls(xs: List[String]): Unit = xs.foreach(println)

}