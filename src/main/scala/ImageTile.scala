import com.sksamuel.scrimage._
import com.sksamuel.scrimage.nio._
import ColorScale._

object ImageTile {
  val void = Image.filled(Tile.size, Tile.size, ColorScale.scholars(0))

  def apply(tile: Tile): ImageTile = tile match {
    case Tile(None      , pos) => ImageTile(void, pos)
    case Tile(Some(data), pos) => {
      val pixels = data.map( ColorScale.scholars(_) : Pixel )
      val image  = Image(Tile.size, Tile.size, pixels.toArray)
      ImageTile(image, pos)
    }
  }

  // debug/test
  def main(args: Array[String]): Unit = {
    implicit val fs = new LocalFs
    fs.readDirectory("data/dem3")
      .zipWithIndex
      .foreach{ case (path,i) =>
        ImageTile(Tile(TileRef(path))).writePng("target/" + i + ".png")
      }
  }
}

case class ImageTile(

  image   : Image,
  position: Coord

) {

  def writePng(path: String)(implicit fs: Fs) =
    fs.writeFile(path, image.bytes(PngWriter.MaxCompression).toVector)

}
