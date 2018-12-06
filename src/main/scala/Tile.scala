import scala.collection.immutable._

object Tile {
  def apply(ref: TileRef): Tile = {
    ref match {
      case TileRef(None      , pos) => Tile(None, pos)
      case TileRef(Some(file), pos) =>
        /* TODO: load data from file */
        Tile(Some(Vector()), pos)
    }
  }
}

case class Tile(
  data    : Option[Vector[Short]],
  position: Coord
)
