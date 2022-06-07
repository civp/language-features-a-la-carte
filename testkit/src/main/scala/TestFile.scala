package testkit

import java.nio.charset.StandardCharsets

import scala.meta.inputs.Input
import scala.meta.internal.io.FileIO
import scala.meta.io.AbsolutePath

class TestFile (
    val name: String,
    val path: AbsolutePath
) {
  def read: Input =
    Input.VirtualFile(name, FileIO.slurp(path, StandardCharsets.UTF_8)) 

  // TODO: write
  
  override def toString: String = path.toString()
}
