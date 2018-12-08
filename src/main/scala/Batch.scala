import org.apache.spark._

object Batch extends App {
  val sc = new SparkContext( new SparkConf()
    .setAppName("mseh").setMaster("local[*]") )

  // WE SHOULD USE org.apache.spark.mllib.rdd.RDDFunctions.sliding
  // INSTEAD OF zipWithIndex/groupBy/...

  // Initial dataset.
  val init = sc
    .parallelize(Directory("data/dem3"))
    // DEBUG: print filenames
    .map{x => println(x) ; x}
    // Parsing coordinates from filenames.
    .map(TileRef(_))
    // Adding first and last blank tiles.
    // FIXME: make it work with TileRef.to
    .union(sc.parallelize(Seq(
      TileRef.min, TileRef.max
    )))
    // Morton order sort (quadtree layout, see Coord.scala).
    .sortBy(identity)
    // Filling holes with blank tiles.
    .flatMap(x => Seq(x,x))
    .zipWithIndex
    .groupBy{case (_,i) => (i + 1) / 2}
    .flatMap{
      case (_, Seq((a,_),(b,_))) => (a to b).tail // FIXME (see TileRef.scala)
      case (0, x)                => x.map(_._1)
      case _                     => Seq()
    }
    // Loading tiles data from files.
    .map(Tile(_))

  // rdd.take(1).length == 1
  // def scaleDown(base: RDD[Tile]) = base
  //   .

  sc.stop
}
