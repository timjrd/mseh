import scala.math._

object ColorScale {
  // Challenger Deep: -11034m
  // Mount Everest  : +8848m

  // Extracted from:
  // https://www.r-bloggers.com/gmt-standard-color-palettes
  val relief = ColorScale(Vector(
    -8000 -> (0,0,0),
    -4000 -> (1,6,16),
    -2000 -> (0,7,30),
    -1000 -> (3,27,63),
    -100  -> (9,74,121),
    -60   -> (16,131,188),
    -30   -> (59,176,173),
    -20   -> (123,222,158),
    -10   -> (176,250,171),
    -5    -> (212,252,212),
     5    -> (56,103,38),
     10   -> (117,98,43),
     20   -> (161,141,56),
     30   -> (210,189,71),
     60   -> (248,228,87),
     100  -> (248,233,107),
     1000 -> (251,235,129),
     2000 -> (251,240,151),
     4000 -> (251,245,180),
     8000 -> (252,250,217)
  ))
}

case class ColorScale(palette: Vector[ Tuple2[Int, Tuple3[Int,Int,Int]] ]) {
  import ColorScale._

  def apply(x: Short) = {
    val pairs = palette.sliding(2).toVector
    val Vector(from,to) = pairs
      .find{ case Vector(lo,hi) => x <= hi._1 }
      .getOrElse(pairs.last)

    val lo    = min(x, from._1)
    val hi    = max(x, to._1)
    val range = hi - lo

    val k = x - lo
    def lerp(a:Int, b:Int) = a*(range-k)/range + b*k/range

    ( lerp(from._2._1, to._2._1),
      lerp(from._2._2, to._2._2),
      lerp(from._2._3, to._2._3) )
  }
}

