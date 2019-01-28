import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.ByteBuffer

import org.apache.spark._
import org.apache.spark.rdd._
import org.apache.spark.mllib.rdd.RDDFunctions._

import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._

object Batch {
  def batch(local: Boolean, dir: String) {
    val c   = new SparkConf().setAppName("mseh")
    val cfg = if (local) c.setMaster("local[*]") else c
    val sc  = new SparkContext(cfg)

    val hcfg   = HBaseConfiguration.create()
    val hadm   = ConnectionFactory.createConnection(hcfg).getAdmin()
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

    def reduce(base: RDD[Tile], zoom: Int): Unit =
      if (zoom >= 0) {
        val tableName = TableName.valueOf(
          prefix + String.format("_%02d", new Integer(zoom+2)) )

        val family    = Array[Byte]('0')
        val qualifier = Array[Byte]('0')

        val table = new HTableDescriptor(tableName)
        table.addFamily(new HColumnDescriptor(family))

        hadm.createTable(table)

        base
          .flatMap(_.split)
          .flatMap(_.split)
          .map(ImageTile(_))
          .foreachPartition{ tiles =>
            val db = ConnectionFactory.createConnection(hcfg)
            tiles.foreach{ tile =>
              val i = ByteBuffer.allocate(Integer.BYTES * 2)
              i.putInt(tile.position.x)
              i.putInt(tile.position.y)
              db.getTable(tableName)
                .put( new Put(i.array())
                  .addColumn(family, qualifier, tile.pngBytes.get) )
            }
            db.close
          }

        reduce(base.sliding(4,4).map(Tile(_)), zoom-1)
      }

    reduce(init, TileRef.zoom)
    println("\nOUTPUT WRITTEN INTO TABLES PREFIXED BY " + prefix + "\n")
  }
}
