package semantic

import java.nio.file.{Files, Paths}

import tastyquery.reader.TastyUnpickler
import tastyquery.ast.Trees._
import tastyquery.ast.Types.{Type, NoType}
import tastyquery.ast.Names._
import tastyquery.Contexts

class Checker(rules: List[Rule]) {

  private def typeOf(tree: Tree): Type = {
    try {
      tree.tpe
    } catch {
      case e: TypeComputationError => {
        // tree.walkTypedTree
        NoType
      }
    }
  }

  type Method = (Type, TermName)

  def check(filename: String): List[Method] = {
      val unpickler = new TastyUnpickler(Files.readAllBytes(Paths.get(filename)))
      val trees = unpickler
        .unpickle(new TastyUnpickler.TreeSectionUnpickler()).get
        .unpickle(using Contexts.empty(filename))
      trees.headOption match {
        case Some(tree) =>
          tree.walkTree[List[Method]]({
            case Select(qualifier, name: SimpleName) =>
              (typeOf(qualifier), name) :: Nil
            case Apply(Ident(name: TermName), args) => name match {
              case dname: DerivedName => (NoType, dname.underlying) :: Nil
              case name: SimpleName => (NoType, name) :: Nil
              case _ => Nil
            }
            case _ => Nil
          })(_ ::: _, Nil)
        case None => Nil
      }
  }
}
