
object TileRef {
  // Zoom level of a raw tile: 9 such as 2^9 > 2^log2(360) . The
  // SRTM-3 dataset is approximately 180*360 but we assume it is a
  // 360*360 square (the missing tiles beeing filled with blank
  // data). The tile size is irrelevant. See
  // https://leafletjs.com/examples/zoom-levels .
  val zoom = 9
  val min  = TileRef(None, Coord(0  , 0  , zoom))
  val max  = TileRef(None, Coord(359, 359, zoom))

  def apply(file: String): TileRef =
    TileRef(Some(file), Coord(file, zoom))
}

case class TileRef(

  file    : Option[String],
  position: Coord

) extends Ordered[TileRef] {

  // Fill the gap with blank tiles.
  // FIXME: this and that are replaced by blank tiles !
  def to(that: TileRef) = (position to that.position).map(TileRef(None, _))

  override def compare(that: TileRef) = position compare that.position
}
