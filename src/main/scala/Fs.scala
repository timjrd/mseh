import org.apache.spark.rdd._
import java.nio.file._
import collection.JavaConverters._

trait Fs {
  def readDirectory(path: String): Seq[String]
  def readFile     (path: String): Vector[Byte]
  def writeFile    (path: String, data: Vector[Byte]): Unit
}

class LocalFs extends Fs {
  override def readDirectory(path: String) =
    Files.list(Paths.get(path))
      .iterator
      .asScala
      .map(_.toString)
      .toSeq

  override def readFile(path: String) =
    Files.readAllBytes(Paths.get(path)).toVector

  override def writeFile(path: String, data: Vector[Byte]) =
    Files.write(Paths.get(path), data.toArray)
}
