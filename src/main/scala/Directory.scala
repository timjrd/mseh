import org.apache.spark.rdd._
import java.nio.file._
import collection.JavaConverters._

object Directory {
  def apply(path: String) =
    // Is it lazy ?
    Files.list(Paths.get(path))
      .iterator
      .asScala
      .map(_.toString)
      .toSeq
}
