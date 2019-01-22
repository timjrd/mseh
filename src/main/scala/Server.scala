import org.http4s._
import org.http4s.dsl._
import org.http4s.server.blaze._
import org.http4s.util.ProcessApp

import scalaz.concurrent.Task
import scalaz.stream.Process

object Main extends ProcessApp {

  def serveFile(path: String, request: Request) =
    StaticFile.fromString("statc/" + path, Some(request))
      .map(Task.now)
      .getOrElse(NotFound())

  val static = HttpService {
    case request @ GET -> Root => serveFile("index.html" , request)
    case request @ GET -> path => serveFile(path.toString, request)
  }

  /* ... */

  override def process(args: List[String]): Process[Task, Nothing] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(static, "/")
      .serve
  }
}
