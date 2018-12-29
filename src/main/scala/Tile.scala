import scala.collection.immutable._

object Tile {
  val rawSize = 1201
  val size    = rawSize - 1
  val void    = Short.MinValue

  def apply(ref: TileRef)(implicit fs: Fs): Tile = ref match {
    case TileRef(None      , pos) => Tile(None, pos)
    case TileRef(Some(file), pos) =>
      val data = fs
        .readFile(file)
        .grouped(2)
        .zipWithIndex
        .flatMap{ case (Vector(hi,lo), i) =>
          if (i / rawSize == 0 || i % rawSize == 0)
            None
          else
            Some(( ((hi & 0xFF) << 8) | (lo & 0xFF) ).toShort)
        }

      Tile(Some(data.toVector), pos)
  }

  def apply(upscaled: Array[Tile]): Tile = {
    def at(x2: Int, y2: Int) = {
      val x0 = x2 / size
      val y0 = y2 / size

      val x1 = x2 - (size*x0)
      val y1 = y2 - (size*y0)

      upscaled(y0*2 + x0)(x1, y1).toInt
    }
    val data = Vector.tabulate(size*size){ i =>
      val x2_0 = (i % size) * 2
      val y2_0 = (i / size) * 2

      val x2_1 = x2_0 + 1
      val y2_1 = y2_0 + 1

      val a = at(x2_0, y2_0)
      val b = at(x2_0, y2_1)
      val c = at(x2_1, y2_0)
      val d = at(x2_1, y2_1)

      ((a + b + c + d) / 4).toShort
    }
    Tile(Some(data), upscaled.head.position.scaleDown)
  }
}

case class Tile(

  data    : Option[Vector[Short]],
  position: Coord

) {

  import Tile._

  def apply(x: Int, y: Int) = data match {
    case Some(dat) => dat(y*size + x)
    case None      => void
  }

}
