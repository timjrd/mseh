
object Main {
  def main(args: Array[String]): Unit = args match {
    case Array("batch", dir) =>
      Batch.batch(false, dir)

    case Array("batch", "--local", dir) =>
      Batch.batch(true, dir)

    case Array("serve", port, prefix) if port forall Character.isDigit =>
      Server.serve(port.toInt, prefix)

    case _ => println("""usage: ... batch [--local] <directory>
                        |       ... serve <port> <prefix>""".stripMargin)
  }
}
