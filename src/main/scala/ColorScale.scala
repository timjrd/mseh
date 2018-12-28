import scala.math._

object ColorScale {
  // Challenger Deep: -11034m
  // Mount Everest  : +8848m
  val halfRange = 8848

  // Extracted from:
  // https://www.r-bloggers.com/gmt-standard-color-palettes
  val relief = ColorScale(Vector(
    (0,0,0),
    (1,6,16),
    (0,7,30),
    (3,27,63),
    (9,74,121),
    (16,131,188),
    (59,176,173),
    (123,222,158),
    (176,250,171),
    (212,252,212),
    (56,103,38),
    (117,98,43),
    (161,141,56),
    (210,189,71),
    (248,228,87),
    (248,233,107),
    (251,235,129),
    (251,240,151),
    (251,245,180),
    (252,250,217)
  ))
}

case class ColorScale(palette: Vector[Tuple3[Int,Int,Int]]) {
  import ColorScale._

  def apply(w: Short) = {
    val lo    = -halfRange
    val hi    = halfRange
    val x     = max(lo, min(w.toInt, hi))
    val range = 2 * halfRange
    val maxI  = palette.length - 1

    val fromI = ((x-lo) * maxI) / range
    val modI  = ((x-lo) * maxI) % range
    val toI   = min(fromI + 1, maxI)

    val from  = palette(fromI)
    val to    = palette(toI)

    def lerp(a:Int, b:Int) = a*(range-modI-1)/(range-1) + b*modI/(range-1)

    ( lerp(from._1, to._1),
      lerp(from._2, to._2),
      lerp(from._3, to._3) )
  }
}

