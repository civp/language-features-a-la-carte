package testkit

import java.nio.charset.StandardCharsets

import scala.meta.inputs.Input
import scala.meta.inputs.Position
import scala.meta.internal.io.FileIO
import scala.meta.io.AbsolutePath

class TestFile (
    val name: String,
    val path: AbsolutePath
) {

  private val input = 
    Input.VirtualFile(name, FileIO.slurp(path, StandardCharsets.UTF_8)) 

  def read: Input = input

  // TODO: write
  
  def getPath: String = path.toString()

  /**
    * Converts a span from `start` to `end` to a position
    * with line and column numbers
    *
    * @param start
    * @param end
    * @return
    */
  def spanToPosition(start: Int, end: Int): Position =
    Position.Range(input, start, end)

}
