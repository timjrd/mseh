import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import scala.concurrent.ExecutionContext.Implicits.global

import fs2._
import org.http4s.server.blaze._

object Server extends StreamApp[IO] {

  def serveFile(path: String, request: Request[IO]) = 
    StaticFile.fromString("static/" + path, Some(request)).getOrElseF(NotFound())

  val static = HttpService[IO] {
    case request @ GET -> Root => serveFile("index.html" , request)
    case request @ GET -> path => serveFile(path.toString, request)
  }

  /* ... */

  override def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(static, "/")
      .serve
}
