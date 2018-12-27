import org.apache.spark.rdd._
import java.nio.file._
import collection.JavaConverters._

trait Fs {
  def directory(path: String): Seq[String]
  def file     (path: String): Vector[Byte]
}

class LocalFs extends Fs {
  override def directory(path: String) =
    Files.list(Paths.get(path))
      .iterator
      .asScala
      .map(_.toString)
      .toSeq

  override def file(path: String) =
    Files.readAllBytes(Paths.get(path)).toVector
}
