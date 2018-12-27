
object TileRef {
  // Zoom level of a raw tile: 9 such as 2^9 > 2^log2(360) . The
  // SRTM-3 dataset is approximately 180*360 but we assume it is a
  // 2^9*2^9 square (the missing tiles beeing filled with blank
  // data). The tile size is irrelevant. See
  // https://leafletjs.com/examples/zoom-levels .
  val zoom     = 9
  val size     = 1 << zoom // 2^9
  val min      = TileRef(None, Coord(0     , 0     , zoom))
  val max      = TileRef(None, Coord(size-1, size-1, zoom))
  val minusOne = TileRef(None, Coord(-1, zoom))

  def apply(file: String): TileRef =
    TileRef(Some(file), Coord(file, zoom))
}

case class TileRef(

  file    : Option[String],
  position: Coord

) extends Ordered[TileRef] {

  // Fill the gap with blank tiles.
  def to(that: TileRef) =
    this +: (position to that.position)
      .drop(1).dropRight(1)
      .map(TileRef(None, _)) :+ that

  override def compare(that: TileRef) = position compare that.position
}
