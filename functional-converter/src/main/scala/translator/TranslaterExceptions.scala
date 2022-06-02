package translator

case class TranslaterException(msg: String) extends Exception(msg)

case class UnexpectedConstructException(obj: Any) extends Exception(s"unexpected: $obj")

