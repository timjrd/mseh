import org.apache.spark._

object Batch extends App {
  val ctx = new SparkContext( new SparkConf()
    .setAppName("mseh").setMaster("local[*]") )

  /* ... */
}
