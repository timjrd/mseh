import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.util.ProcessApp
import java.nio.ByteBuffer

import scalaz.concurrent.Task
import scalaz.stream.Process
import java.io.File

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{CellUtil, HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.conf.Configuration

object Main extends ProcessApp {

  def printRow(result : Result) = {
      val cells = result.rawCells();
      print( Bytes.toString(result.getRow) + " : " )
      for(cell <- cells){
        val col_name = Bytes.toString(CellUtil.cloneQualifier(cell))
        val col_value = Bytes.toString(CellUtil.cloneValue(cell))
        print("(%s,%s) ".format(col_name, col_value))
      }
      println()
  }

  def serveFile(path: String, request: Request) =
    StaticFile.fromString("static/" + path, Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  val static = HttpService {
    case request @ GET -> Root => {
      println("bonjour")
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


        val prefix = "mseh_2019.01.24.15.09.25"
        val tableName = TableName.valueOf(prefix + String.format("_%02d", new Integer(zoom)))
        println(tableName)
        val table = connection.getTable(tableName)


        val i = ByteBuffer.allocate(Integer.BYTES * 2)
        i.putInt(x)
        i.putInt(y)
        
        var get = new Get(i.array())
        var result = table.get(get)
        printRow(result)

      }
      serveFile(path.toString, request)
    }
  }

  /* ... */

  override def process(args: List[String]): Process[Task, Nothing] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(static, "/")
      .serve
  }
}
