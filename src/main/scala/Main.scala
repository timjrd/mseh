
object Main {
  def main(args: Array[String]): Unit = args match {
    case Array("batch") =>
      Batch.batch(false)

    case Array("batch", "--local") =>
      Batch.batch(true)

    case Array("serve", port, prefix) if port forall Character.isDigit =>
      Server.serve(port.toInt, prefix)

    case _ => println("""usage: ... batch
                        |       ... batch --local
                        |       ... serve <port> <prefix>""".stripMargin)
  }
}
