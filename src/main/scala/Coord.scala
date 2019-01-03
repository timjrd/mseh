import scala.math._
import Coord._

object Coord {
  def apply(file: String, zoom: Int): Coord = {
    // file name example: /user/raw_data/dem3/N11W074.hgt
    // command to test:  Coord("/user/raw_data/dem3/N11W074.hgt", 1)
    var x_is_pos_deg = 0
    var y_is_pos_deg = 0
    var x_is_pos = 1
    var y_is_pos = 1
    var tmp_x = 0
    var tmp_y = 0
    val tmp = file.split("/")
    val tmp_2 = tmp(tmp.length-1)
    val splited = tmp_2.split("\\.")
    // We test if the coordonate are positive (i.e. if the tile is in the north or east)
    if (splited(0).contains("W") || splited(0).contains("w")) {
      x_is_pos_deg = 360
      x_is_pos = -1
    }
    if (splited(0).contains("S") || splited(0).contains("s")) {
      y_is_pos_deg = 180
      y_is_pos = -1
    }
    // To get the longitude coordonate
    tmp_x = toInt(splited(0).substring(4))
    // To get the latitude coordonate
    tmp_y = toInt(splited(0).substring(1,3))

    val x = x_is_pos_deg + tmp_x * x_is_pos 
    val y = y_is_pos_deg + tmp_y * y_is_pos

    Coord(x, y, zoom)
  }

  // To allow to cast string to int w/o Exception
  def toInt(s: String): Int = {
    try {
      s.toInt
    } catch {
      case e: Exception => 0
    }
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

  def scaleDown = Coord(x/2, y/2, z-1)

  override def compare(that: Coord) = morton compare that.morton
}
