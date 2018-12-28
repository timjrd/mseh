import scala.math._

object ColorScale {
  // Challenger Deep: -11034m
  // Mount Everest  : +8848m
  // Paris          : +78m
  // Chamonix       : +995m

  // Colors from:
  // https://www.color-hex.com/color-palette/67502
  val scholars = ColorScale(Seq(
    -500  -> (43,89,90),
    -1    -> (153,175,93),
    -0    -> (98,161,169),
    +1    -> (153,175,93),
    +100  -> (230,183,64),
    +1000 -> (200,61,50),
    +8000 -> (255,255,255)
  ))

  // Colors from:
  // https://www.color-hex.com/color-palette/70012
  val technical = ColorScale(Seq(
    -400  -> (17,36,77),
    -50   -> (36,76,163),
    -0    -> (86,141,216),
    +1    -> (36,122,59),
    +100  -> (229,111,30),
    +1000 -> (210,38,38),
    +8000 -> (255,255,255)
  ))

  // Colors from:
  // https://www.r-bloggers.com/gmt-standard-color-palettes
  val gmtRelief = ColorScale(Seq(
    -400  -> (0,0,0),
    -1    -> (150,150,150),
    -0    -> (212,252,212),
    +1    -> (56,103,38),
    +100  -> (117,98,43),
    +1000 -> (248,228,87),
    +8000 -> (255,255,255)
  ))
}

case class ColorScale(palette: Seq[ Tuple2[Int, Tuple3[Int,Int,Int]] ]) {
  import ColorScale._

  private val pairs = palette.toArray.sliding(2).toArray

  def apply(x: Short) = {
    val Array(from,to) = pairs
      .find{ case Array(lo,hi) => x <= hi._1 }
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

