package testkit

object Report {
  
  /**
    * Format line, column, and message to a string,
    * which will be used for reporting a violation
    */
  def format(line: Int, column: Int, msg: String): String =
    s"${line}:${column} => ${msg}"

}
