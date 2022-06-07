package res

class ListHead {

  def getHead(list: List[Int]): Int =
    list.head
  
  def getHead(set: Set[Int]): Int = {
    // val set = Set(1, 2, 3)
    // set.toList.head is not supported due to the implementation of resolveToSymbol
    val lst = set.toList
    lst.head
  }

}
