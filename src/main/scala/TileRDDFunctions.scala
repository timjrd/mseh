import org.apache.spark.rdd.RDD

object TileRDDFunctions {
  implicit def wrap(rdd: RDD[Tile]) = new TileRDDFunctions(rdd)
}

class TileRDDFunctions(rdd: RDD[Tile]) {
  def scale(factor: Float): RDD[Tile] = null
}


