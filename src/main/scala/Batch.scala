import org.apache.spark._

object Batch extends App {
  val ctx = new SparkContext( new SparkConf()
    .setAppName("mseh").setMaster("local[*]") )

  // initial dataset
  val all = ctx
    .parallelize(Directory("data/dem3"))
    // DEBUG: print filenames
    .map{x => println(x) ; x}
    // parsing coordinates from filenames
    .map(TileRef(_))
    // morton order sort (quadtree layout)
    .sortBy(identity)
    // filling holes with empty tiles
    .flatMap(x => Seq(x,x))
    .zipWithIndex
    .groupBy{case (_,i) => (i + 1) / 2}
    .flatMap{
      case (_, Seq((a,_),(b,_))) => (a to b).tail
      case (0, x)                => x.map(_._1)
      case _                     => Seq()
    }
    // loading tiles data from files
    .map(Tile(_))

  // rdd.take(1).length == 1
  // def scaleDown(base: RDD[Tile]) = base
  //   .

  ctx.stop
}
