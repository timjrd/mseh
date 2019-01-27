import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.util.ProcessApp
import org.http4s.headers._
import java.nio.ByteBuffer

import scalaz.concurrent.Task
import scalaz.stream.Process
import java.io.File

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.conf.Configuration

object Server extends ProcessApp {

  def serveFile(path: String, request: Request) =
    StaticFile.fromString("static/" + path, Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  val static = HttpService {
    case request @ GET -> Root =>
      serveFile("index.html", request)
    case request @ GET -> path =>
      serveFile(path.toString(), request)
  }

  def tiles(prefix: String) =
    HttpService {
      case GET -> Root / z_ / x_ / y_ => {
        val z = Integer.parseInt(z_)
        val x = Integer.parseInt(x_)
        val y = Integer.parseInt(y_)

        val conf : Configuration = HBaseConfiguration.create()
        val connection = ConnectionFactory.createConnection(conf)

        val tableName = TableName.valueOf(prefix + String.format("_%02d", new Integer(z)))
        val table = connection.getTable(tableName)

        val i = ByteBuffer.allocate(Integer.BYTES * 2)
        i.putInt(x)
        i.putInt(y)
        
        val result = table.get(new Get(i.array))
        Ok(result.value, Headers(Header("Content-Type", "image/png")))
      }
    }

  override def process(args: List[String]): Process[Task, Nothing] = {    
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(static, "/")
      .mountService(tiles(args(0)), "/tiles")
      .serve
  }
}
