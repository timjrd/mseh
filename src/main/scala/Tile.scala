import scala.collection.immutable._
import Tile._

object Tile {
  val rawSize = 1201

  def apply(ref: TileRef)(implicit fs: Fs): Tile = ref match {
    case TileRef(None      , pos) => Tile(None, rawSize-1, pos)
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

      Tile(Some(data.toVector), rawSize-1, pos)
  }

  def apply(upscaled: Array[Tile]): Tile = {
    val size = upscaled.head.size
    val pos  = upscaled.head.position.scaleDown

    if (upscaled.forall(_.data == None))
      Tile(None, size, pos)

    else {
      def at(x2: Int, y2: Int) = {
        val x0 = x2 / size
        val y0 = y2 / size

        val x1 = x2 - (size*x0)
        val y1 = y2 - (size*y0)

        upscaled(y0*2 + x0)(x1, y1)
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

        ((a._1 + b._1 + c._1 + d._1) / (a._2 + b._2 + c._2 + d._2)).toShort
      }
      Tile(Some(data), size, pos)
    }
  }
}

case class Tile(
  data    : Option[Vector[Short]],
  size    : Int,
  position: Coord
) {
  def apply(x: Int, y: Int) = data match {
    case Some(dat) => dat(y*size + x) match {
      case Short.MinValue => (0,0)
      case v => (v.toInt,1)
    }
    case None => (0,0)
  }

  def split = Seq(
    Tile(slice(0     , 0     , size/2, size/2), size/2, position.scaleUp._1),
    Tile(slice(size/2, 0     , size/2, size/2), size/2, position.scaleUp._2),
    Tile(slice(0     , size/2, size/2, size/2), size/2, position.scaleUp._3),
    Tile(slice(size/2, size/2, size/2, size/2), size/2, position.scaleUp._4)
  )

  private def slice(x: Int, y: Int, w: Int, h: Int) = data.map(dat =>
    Vector.tabulate(w*h){ i =>
      val x_ = x + i % w
      val y_ = y + i / w
      dat(y_ * size + x_)
    })
}
