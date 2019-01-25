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

  def run(prefix: String) =
    HttpService {
      case request @ GET -> Root => {
        serveFile("index.html" , request)
      }
      case request @ GET -> path => {
        val values = path.toString.split('/')
        if(values.length == 4){
          val zoom = values(1).toInt
          val x = values(2).toInt
          val y = values(3).toInt

          val conf : Configuration = HBaseConfiguration.create()
          val connection = ConnectionFactory.createConnection(conf)

          //val prefix = "mseh_2019.01.24.15.09.25"
          val tableName = TableName.valueOf(prefix + String.format("_%02d", new Integer(zoom)))
          val table = connection.getTable(tableName)

          val i = ByteBuffer.allocate(Integer.BYTES * 2)
          i.putInt(x)
          i.putInt(y)
          
          var get = new Get(i.array())
          var result = table.get(get)
          Ok(result.value(), Headers(Header("Content-Type", "image/png")))

        } else
          //BadRequest().withBody("Path must be domain://zoom/x_position/y_position")
          serveFile(path.toString(), request)
      }
    }

  override def process(args: List[String]): Process[Task, Nothing] = {    
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(run(args(0)), "/")
      .serve   
    
  }
}
