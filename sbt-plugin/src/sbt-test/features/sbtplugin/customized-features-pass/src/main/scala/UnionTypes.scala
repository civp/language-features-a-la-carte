/**
  * Union Types: https://dotty.epfl.ch/docs/reference/new-types/union-types.html
  */
object UnionTypes:

  sealed trait Division
  final case class DivisionByZero(msg: String) extends Division
  final case class Success(double: Double) extends Division

  // You can create type aliases for your union types (sum types).
  type DivisionResult = DivisionByZero | Success
