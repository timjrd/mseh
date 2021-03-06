import collection.JavaConverters._

import sun.misc.IOUtils;
import java.nio.file.{Files, Paths}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

trait Fs extends Serializable {
  def readDirectory(path: String): Seq[String]
  def readFile     (path: String): Vector[Byte]
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
}

class HadoopFs extends Fs {
  private def fs = FileSystem.get(new Configuration)

  private def toList(it: RemoteIterator[LocatedFileStatus]): List[String] = {
    var res = List[String]()
    while (it.hasNext)
      res = it.next.getPath.toString :: res
    res
  }

  override def readDirectory(path: String) =
    toList( fs.listFiles(new Path(path), false) )

  override def readFile(path: String) = {
    val stream = fs.open(new Path(path))
    val res    = IOUtils.readFully(stream, -1, false).toVector
    stream.close
    res
  }
}
