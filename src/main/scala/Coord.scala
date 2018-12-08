import scala.math._

object Coord {
  def apply(file: String, zoom: Int): Coord = {
    // TODO: parse coordinates from filename

    val x = 0
    val y = 0

    Coord(x, y, zoom)
  }

  def apply(morton: Int, zoom: Int): Coord =
    Coord( Compact1By1(morton >> 0), Compact1By1(morton >> 1), zoom)

  // Stolen from
  // https://fgiesen.wordpress.com/2009/12/13/decoding-morton-codes .
  def Part1By1(x0: Int) = { // Int is 32 bits
    val x1 = x0 & 0x0000ffff
    val x2 = (x1 ^ (x1 << 8)) & 0x00ff00ff
    val x3 = (x2 ^ (x2 << 4)) & 0x0f0f0f0f
    val x4 = (x3 ^ (x3 << 2)) & 0x33333333
    val x5 = (x4 ^ (x4 << 1)) & 0x55555555
    x5
  }
  def Compact1By1(x0: Int) = { // Int is 32 bits
    val x1 = x0 & 0x55555555
    val x2 = (x1 ^ (x1 >> 1)) & 0x33333333
    val x3 = (x2 ^ (x2 >> 2)) & 0x0f0f0f0f
    val x4 = (x3 ^ (x3 >> 4)) & 0x00ff00ff
    val x5 = (x4 ^ (x4 >> 8)) & 0x0000ffff
    x5
  }
}

case class Coord(

  // Top-left origin.
  x: Int, // longitude (as expected by Leaflet)
  y: Int, // latitude  (as expected by Leaflet)
  z: Int  // zoom      (as expected by Leaflet)

) extends Ordered[Coord] {

  import Coord._

  // val size = 1 << z // 2^z

  // Morton code. This enables sorting a sequence of tiles (such as a
  // RDD[Tile]) according to the order one would get from a
  // depth-first traversal of a quadtree. See
  // https://en.wikipedia.org/wiki/Z-order_curve .
  val morton = (Part1By1(y) << 1) + Part1By1(x)

  // Fill the gap with blank tiles.
  def to(that: Coord) =
    min(morton, that.morton)
      .to(max(morton, that.morton))
      .map(Coord(_,z))

  override def compare(that: Coord) = morton compare that.morton
}
