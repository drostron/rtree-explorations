package ndimrtree

// building towards parity with the archery benchmark

import ichi.bench.Thyme
import NDimRTreeOps._
import scala.util.Random.nextGaussian
import shapeless._
import spire._, implicits._

object Main {

  type N = Float :: Float :: HNil

  val xmin, ymin = -5000F
  val xmax, ymax = 5000F
  val dx, dy = 10000F
  // val size = 1000000
  val size = 50000
  val num = 1000
  val radius = 10

  val entries = (0 until size).map(n => Entry(n, nextPoint)).toArray
  val extra = (0 until num).map(n => Entry(n + size, nextPoint)).toArray
  val boxes = (0 until num).map(_ => nextBox(radius))

  // cluster points around (0, 0)
  def nextPoint: Point[N] =
    Point(1000F * nextF :: 1000F * nextF :: HNil)

  // generate box with radius r
  def nextBox(r: Int): Box[N] = {
    val Point(x :: y :: HNil) = nextPoint
    Box(x - r :: y - r :: HNil, x + r :: y + r :: HNil)
  }

  object archerySeeds {
    import archery._

    val entries = (0 until size).map(n => Entry(nextPoint, n)).toArray
    val extra = (0 until num).map(n => Entry(nextPoint, n + size)).toArray
    val boxes = (0 until num).map(_ => nextBox(radius))

    // cluster points around (0, 0)
    def nextPoint: Point =
      Point(1000F * nextF, 1000F * nextF)

    // generate box with radius r
    def nextBox(r: Int): Box = {
      val Point(x, y) = nextPoint
      Box(x - r, y - r, x + r, y + r)
    }

  }

  // generate values in [-5F, 5F], mean 0F with stddev 1F
  def nextF: Float = {
    val n = nextGaussian.toFloat
    if (n < -5F) -5F else if (n > 5F) 5F else n
  }

  def main(args: Array[String]): Unit = {
    val th = Thyme.warmedBench(verbose = print(_: String))

    println(s"\narchery: building tree from $size entries")
    val art = th.pbench {
      archery.RTree(archerySeeds.entries: _*)
    }

    println(s"\nndimrtree: building tree from $size entries")
    val nrt = th.pbench {
      RTree(entries)
    }

    println(s"\narchery: doing $num random searches (radius: $radius)")
    val an1 = th.pbench {
      archerySeeds.boxes.foldLeft(0)((n, b) => n + art.search(b).length)
    }
    println(s"  found $an1 results")

    println(s"\nndimrtree: doing $num random searches (radius: $radius)")
    val nn1 = th.pbench {
      boxes.foldLeft(0)((n, b) => n + nrt.search(b).length)
    }
    println(s"  found $nn1 results")

    println(s"\narchery: doing $num random searches with filter (radius: $radius)")
    val anx = th.pbench {
      archerySeeds.boxes.foldLeft(0)((n, b) => n + art.search(b, _ => true).length)
    }
    println(s"found $anx results")

    println(s"\nndimrtree: doing $num random searches with filter (radius: $radius)")
    val nnx = th.pbench {
      boxes.foldLeft(0)((n, b) => n + nrt.search(b, _ => true).length)
    }
    println(s"found $nnx results")

    println(s"\narchery: doing $num counts")
    val an2 = th.pbench {
      archerySeeds.boxes.foldLeft(0)((n, b) => n + art.count(b))
    }
    println(s"found $an2 results")

    println(s"\nndimrtree: doing $num counts")
    val nn2 = th.pbench {
      boxes.foldLeft(0)((n, b) => n + nrt.count(b))
    }
    println(s"found $nn2 results")

    println(s"\narchery: removing $num entries")
    th.pbench {
      archerySeeds.entries.take(num).foldLeft(art)(_ remove _)
    }

    println(s"\nndimrtree: removing $num entries")
    th.pbench {
      entries.take(num).foldLeft(nrt)(_ remove _)
    }

    println(s"\narchery: inserting $num entries")
    th.pbench {
      archerySeeds.extra.foldLeft(art)(_ insert _)
    }

    println(s"\nndimrtree: inserting $num entries")
    th.pbench {
      extra.foldLeft(nrt)(_ add _)
    }

    ()
  }

}
