package testkit

import java.nio.charset.StandardCharsets

import scala.meta.inputs.Input
import scala.meta.internal.io.FileIO
import scala.meta.io.AbsolutePath
import scala.meta.io.RelativePath

class TestPath (
    val name: String,
    val path: AbsolutePath
) {
  def read: Input =
    Input.VirtualFile(name, FileIO.slurp(path, StandardCharsets.UTF_8)) 
  
  override def toString: String = path.toString()
}
