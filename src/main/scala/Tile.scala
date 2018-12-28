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
            Some((hi << 8 | lo).toShort)
        }

      Tile(Some(data.toVector), pos)
  }

  def apply(upscaled: Array[Tile]): Tile = {
    /* TODO: scale down (with linear interpolation) */
    Tile(Some(Vector()), upscaled.head.position.scaleDown)
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
