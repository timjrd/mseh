
object TileRef {
  def apply(file: String): TileRef =
    TileRef(Some(file), Coord(file, 10)) // WARNING: 10 is *not* the correct value
}

case class TileRef(

  file    : Option[String],
  position: Coord

) extends Ordered[TileRef] {

  def to(that: TileRef) = (position to that.position).map(TileRef(None, _))

  override def compare(that: TileRef) = position compare that.position
}
