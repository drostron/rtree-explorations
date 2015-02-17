package ndimrtree

import NDimRTree._, NDimRTreeOps._
import org.scalacheck._, Arbitrary._, Prop._, Shapeless._
import scala.collection.mutable.ArrayBuffer
import shapeless.{ :: => :×:, _ }
import spire._, implicits.{eqOps => _, _}
import scalaz.{ Ordering => _, _ }, Scalaz._

// tests are directly from or inspired by : https://github.com/meetup/archery/blob/master/core/src/test/scala/rtree.scala

object NDimRTreeTest extends Properties("NDimRTree") {

  // TODO : generic hlist type parameter shape?, coproduct for values, ...

  type V = String
  type N = Int :×: Double :×: String :×: HNil

  // unable to use scalaz's Set Equal instance without an available scalaz.Order instance for T
  implicit def setEqual[T] = Equal.equalA[Set[T]]

  // TODO : define a reasonable distance function
  implicit object dist extends Distance[N] {
    def distance(a: Point[N], b: Point[N]): Double = 7.0
  }

  property("insert entry") = forAll { (r: RTree[V, N], e: Entry[V, N]) =>
    r.add(e).find(e.point) === e.some
  }

  property("build from list of entries") = forAll { entries: List[Entry[V, N]] =>
    RTree(entries).entries.toSet === entries.toSet
  }

  property("rtree.contains works") = forAll { (es: List[Entry[V, N]], e: Entry[V, N]) =>
    val rt = RTree(es)

    es.forall(rt.contains) && (rt.contains(e) === es.contains(e))
  }

  property("rtree.remove works") = forAll { es: List[Entry[V, N]] =>
    val rt = RTree(es)

    val rt2 = es.foldLeft(rt)(_ remove _)

    rt2.entries.isEmpty
  }

  def shuffle[A](buf: ArrayBuffer[A]): Unit = {
    for (i <- 1 until buf.length) {
      val j = scala.util.Random.nextInt(i)
      val t = buf(i)
      buf(i) = buf(j)
      buf(j) = t
    }
  }

  property("rtree.remove out-of-order") = forAll { (es: List[Entry[V, N]]) =>
    val buf = ArrayBuffer(es: _*)
    shuffle(buf)
    var rt = RTree(es)
    while (buf.nonEmpty) {
      require(buf.toSet === rt.entries.toSet)
      val x = buf.remove(0)
      rt = rt.remove(x)
    }
    buf.toSet === rt.entries.toSet
  }

  // TODO : def bound should expand bounds for various types

  // val mile = 1600F
  //
  // def bound(g: Geom, n: Int): Box = {
  //   val d = 10F * mile
  //   Box(g.x - d, g.y - d, g.x2 + d, g.y2 + d)
  // }
  //
  // property("rtree.search/count ignores nan/inf") {
  //   forAll { (es: List[Entry[Int]], p: Point) =>
  //     val rt = build(es)
  //     val nil = Seq.empty[Entry[Int]]
  //
  //     rt.search(Box(Float.PositiveInfinity, 3F, 9F, 9F)) shouldBe nil
  //     rt.search(Box(2F, Float.NaN, 9F, 9F)) shouldBe nil
  //     rt.search(Box(2F, 3F, Float.NegativeInfinity, 9F)) shouldBe nil
  //     rt.search(Box(2F, 3F, 9F, Float.NaN)) shouldBe nil
  //
  //     rt.count(Box(Float.PositiveInfinity, 3F, 9F, 9F)) shouldBe 0
  //     rt.count(Box(2F, Float.NaN, 9F, 9F)) shouldBe 0
  //     rt.count(Box(2F, 3F, Float.NegativeInfinity, 9F)) shouldBe 0
  //     rt.count(Box(2F, 3F, 9F, Float.NaN)) shouldBe 0
  //   }
  // }

  // TODO : review; noticeably modified  from original property("rtree.search works")
  property("rtree.search agrees with withinBox(box, point) filtering") = forAll {
    (es: List[Entry[V, N]], p1: Point[N], p2: Point[N]) =>

    val rt = RTree(es)

    val box1 = initBox(p1, p2)
    require(rt.search(box1).toSet === es.filter(e => withinBox(box1, e.point)).toSet)

    es.foreach { e =>
      val box2 = initBox(e.point, p2)
      require(rt.search(box2).toSet === es.filter(e => withinBox(box2, e.point)).toSet)
    }
    true
  }

  // TODO : property("rtree.searchIntersection works")

  // property("rtree.nearest works") = forAll { (es: List[Entry[V, N]], p: Point[N]) =>
    // val rt = RTree(es)
    //
    // if (es.isEmpty) {
    //   require(rt.nearest(p).isEmpty)
    // }
    // else {
    //   val e = es.min(Ordering.by((e: Entry[V, N]) => e.point.distance(p)))
    //   val d = e.point.distance(p)
    //   // it's possible that several points are tied for closest
    //   // in these cases, the distances still must be equal.
    //   println(s"rt : $rt")
    //   println(s"rt.nearest(p) : ${rt.nearest(p)}")
    //   require(rt.nearest(p).map(_.point.distance(p)) === Some(d))
    // }
    // true
  // }

  // property("rtree.nearestK works") = forAll { (es: List[Entry[V, N]], p: Point[N], k0: Int) =>
    // val k = (k0 % 1000).abs
    // val rt = build(es)
    //
    // val as = es.map(_.geom.distance(p)).sorted.take(k).toVector
    // val bs = rt.nearestK(p, k).map(_.geom.distance(p))
    // as shouldBe bs
    // true
  // }

  sealed trait Action {
    def test(rt: RTree[V, N]): RTree[V, N]
    def control(es: List[Entry[V, N]]): List[Entry[V, N]]
  }

  object Action {
    def run(rt: RTree[V, N], es: List[Entry[V, N]])(as: List[Action]): Unit =
      as match {
        case a :: as =>
          val rt2 = a.test(rt)
          val es2 = a.control(es)
          require(rt2.entries.toSet === es2.toSet)
          run(rt2, es2)(as)
        case Nil =>
          ()
      }
  }

  case class Insert(e: Entry[V, N]) extends Action {
    def test(rt: RTree[V, N]): RTree[V, N] =
      rt.add(e)
    def control(es: List[Entry[V, N]]): List[Entry[V, N]] =
      e :: es
  }

  case class Remove(e: Entry[V, N]) extends Action {
    def test(rt: RTree[V, N]): RTree[V, N] =
      if (rt.contains(e)) rt.remove(e) else rt
    def control(es: List[Entry[V, N]]): List[Entry[V, N]] =
      es match {
        case Nil => Nil
        case `e` :: t => t
        case h :: t => h :: control(t)
      }
  }

  implicit val arbaction = Arbitrary(for {
    e <- arbitrary[Entry[V, N]]
    b <- arbitrary[Boolean]
  } yield {
    val a: Action = if (b) Insert(e) else Remove(e)
    a
  })

  property("ad-hoc rtree") = forAll { (es: List[Entry[V, N]], as: List[Action]) =>
    Action.run(RTree(es), es)(as)
    true
  }

}
