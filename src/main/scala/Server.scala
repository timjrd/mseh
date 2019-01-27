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

object Server {
  def serveFile(path: String, request: Request) =
    StaticFile.fromResource(path, Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  val static = HttpService {
    case request @ GET -> Root =>
      serveFile("/index.html", request)
    case request @ GET -> path =>
      serveFile(path.toString(), request)
  }

  def tiles(prefix: String) = {
    val db = ConnectionFactory.createConnection
    HttpService {
      case GET -> Root / z_ / x_ / y_ => {
        val z = z_.toInt
        val x = x_.toInt
        val y = y_.toInt

        val tableName = TableName.valueOf(prefix + String.format("_%02d", new Integer(z)))

        val i = ByteBuffer.allocate(Integer.BYTES * 2)
        i.putInt(x)
        i.putInt(y)
        
        val tile = db
          .getTable(tableName)
          .get(new Get(i.array))
          .value

        Ok(tile, Headers(Header("Content-Type", "image/png")))
      }
    }
  }

  def serve(port: Int, prefix: String) = BlazeBuilder
    .bindHttp(port)
    .mountService(tiles(prefix), "/tiles")
    .mountService(static, "/")
    .run
    .awaitShutdown
}
