import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.util.ProcessApp

import scalaz.concurrent.Task
import scalaz.stream.Process

import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark._
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client.Scan

object Main extends ProcessApp {

  def serveFile(path: String, request: Request) =
    StaticFile.fromString("statc/" + path, Some(request))
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
        val zoom = values(1)
        val x = values(2)
        val y = values(3)

        val sc = new SparkContext( new SparkConf().setAppName("mseh").setMaster("local[*]"))
        println("youpi")

        val hbaseContext = new HBaseContext(sc, HBaseConfiguration.create())
        var scan = new Scan()
        scan.setCaching(100)

        var getRdd = hbaseContext.hbaseRDD(TableName.valueOf("mseh_2019.01.23.16.11.11_09"), scan)

        //getRdd.foreach(v => println("salutations"))
        getRdd.foreach(v => println(Bytes.toString(v._1.get())))



        ///faire ici la requete vers hbase
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
