import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.mllib.rdd.RDDFunctions._

object Batch extends App {
  val sc = new SparkContext( new SparkConf()
    .setAppName("mseh").setMaster("local[*]") )

  implicit val fs = new LocalFs

  // Initial dataset.
  val init = sc
    .parallelize(fs.readDirectory("data/dem3"))
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

  def scaleDown(base: RDD[Tile]) = base
    .sliding(4,4)
    .map(Tile(_))

  def reduce(base: RDD[Tile], zoom: Int): Tile =
    if (zoom == 0)
      base.first
    else
      reduce(scaleDown(base), zoom - 1)

  ImageTile(reduce(init, TileRef.zoom)).writePng("/tmp/world.png")

  sc.stop
}
