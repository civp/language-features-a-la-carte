/*
mode = blacklist
rules = [
  NoOptionGet
]
*/

package res

class OptionGet {

  def get(i: Option[Int]): Int = i.get /*
                                 ^^^^^
  Option#get is unsafe */

}
