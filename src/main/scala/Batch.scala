import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.ByteBuffer

import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.mllib.rdd.RDDFunctions._

import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.spark.HBaseContext

object Batch {
  def batch(local: Boolean, dir: String) {
    val c   = new SparkConf().setAppName("mseh")
    val cfg = if (local) c.setMaster("local[*]") else c
    val sc  = new SparkContext(cfg)

    val hcfg   = HBaseConfiguration.create()
    val hadm   = ConnectionFactory.createConnection(hcfg).getAdmin()
    val hc     = new HBaseContext(sc, hcfg)
    val prefix = "mseh_" + LocalDateTime.now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"))

    implicit val fs = if (local) new LocalFs else new HadoopFs

    // Initial dataset.
    val init = sc
      .parallelize(fs.readDirectory(dir))
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

    (TileRef.zoom to 0 by -1).foldLeft(init){ case (base,zoom) =>
      val images = base
        .flatMap(_.split)
        .flatMap(_.split)
        .map(ImageTile(_))
        .filter(_.image != None)

      val tableName = TableName.valueOf(
        prefix + String.format("_%02d", new Integer(zoom+2)) )

      val family    = Array[Byte]('0')
      val qualifier = Array[Byte]('0')

      val table = new HTableDescriptor(tableName)
      table.addFamily(new HColumnDescriptor(family))

      hadm.createTable(table)

      hc.bulkPut(images, tableName,
        { tile: ImageTile =>
          val i = ByteBuffer.allocate(Integer.BYTES * 2)
          i.putInt(tile.position.x)
          i.putInt(tile.position.y)
          new Put(i.array()).addColumn(family, qualifier, tile.pngBytes.get)
        })

      base.sliding(4,4).map(Tile(_))
    }

    println("\nOUTPUT WRITTEN INTO TABLES PREFIXED BY " + prefix + "\n")
  }
}
