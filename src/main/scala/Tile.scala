import scala.collection.immutable._

object Tile {
  val size = 1200

  def apply(ref: TileRef): Tile = {
    ref match {
      case TileRef(None      , pos) => Tile(None, pos)
      case TileRef(Some(file), pos) =>
        /* TODO: load data from file */
        Tile(Some(Vector()), pos)
    }
  }

  def apply(upscaled: Array[Tile]): Tile = {
    /* TODO: scale down (with linear interpolation) */
    Tile(Some(Vector()), upscaled.head.position.scaleDown)
  }
}

case class Tile(
  data    : Option[Vector[Short]],
  position: Coord
)
