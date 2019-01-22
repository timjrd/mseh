import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.ByteBuffer

import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.mllib.rdd.RDDFunctions._

import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.spark.HBaseContext
import org.apache.hadoop.hbase.client.Put

object Batch extends App {
  val sc = new SparkContext( new SparkConf()
    .setAppName("mseh").setMaster("local[*]") )

  val hc = new HBaseContext(sc, HBaseConfiguration.create())
  val prefix = "mseh_" + LocalDateTime.now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"))

  implicit val fs = new LocalFs

  // Initial dataset.
  val init = sc
    .parallelize(fs.readDirectory("data/dem3"))
    // Parsing coordinates from filenames.
    .map(TileRef(_))
    // Adding first and last blank tiles.
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

  def reduce(base: RDD[Tile], zoom: Int): Unit =
    if (zoom >= 0) {
      val images = base
        .flatMap(_.split)
        .flatMap(_.split)
        .map(ImageTile(_))
        .filter(_.image != None)

      hc.bulkPut(images,
        TableName.valueOf(prefix + "_" + String.format("%02d", new Integer(zoom))),
        { tile: ImageTile =>
          val i = ByteBuffer.allocate(Integer.BYTES * 2)
          i.putInt(tile.position.x)
          i.putInt(tile.position.y)
          new Put(i).addColumn(Array(), Array(), tile.pngBytes.get)
        })

      reduce(base.sliding(4,4).map(Tile(_)), zoom-1)
    }

  reduce(init, TileRef.zoom)
  sc.stop
  println("\nOUTPUT WRITTEN INTO TABLES " + prefix + "_*\n")
}
