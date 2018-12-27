import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.mllib.rdd.RDDFunctions._

object Batch extends App {
  val sc = new SparkContext( new SparkConf()
    .setAppName("mseh").setMaster("local[*]") )

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
      TileRef.minusOne, TileRef.max
    )))
    // Morton order sort (quadtree layout, see Coord.scala).
    .sortBy(identity)
    // Filling holes with blank tiles.
    .sliding(2)
    .flatMap{ case Array(a,b) => (a to b).tail }
    // Loading tiles data from files.
    .map(Tile(_))

  // rdd.take(1).length == 1
  def scaleDown(base: RDD[Tile]) = base
    .sliding(4,4)
    .map(Tile(_))

  sc.stop
}
