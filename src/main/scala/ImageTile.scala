import com.sksamuel.scrimage._
import com.sksamuel.scrimage.nio._
import ColorScale._

object ImageTile {
  def apply(tile: Tile): ImageTile = tile match {
    case Tile(None      , _   , pos) => ImageTile(None, pos)
    case Tile(Some(data), size, pos) => {
      val pixels = data.map( ColorScale.scholars(_) : Pixel )
      val image  = Image(size, size, pixels.toArray)
      ImageTile(Some(image), pos)
    }
  }
}

case class ImageTile(
  image   : Option[Image],
  position: Coord
) {
  def pngBytes = image.map(x => x.bytes(PngWriter.MaxCompression))
}
