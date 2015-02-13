package twodimrtree

import scalaz._, Scalaz._

case class P[T](x: T, y: T)
case class B[T](xl: T, xu: T, yl: T, yu: T)

object P {
  implicit def equalInstance[T] = Equal.equalA[P[T]]
}

trait Ops[T] {
  def initBound(point1: P[T], point2: P[T]): B[T]
  def expandBound(bound1: B[T], bound2: B[T]): B[T]
  def withinBound(bound: B[T], point: P[T]): Boolean
}

class NumericOps[T : Numeric] extends Ops[T] {
  val numeric = implicitly[Numeric[T]]
  import numeric._

  def initBound(point1: P[T], point2: P[T]): B[T] = expandBound(
    B[T](point1.x, point1.x, point1.y, point1.y),
    B[T](point2.x, point2.x, point2.y, point2.y))
  def expandBound(bound1: B[T], bound2: B[T]): B[T] = B(
    min(bound1.xl, bound2.xl), max(bound1.xu, bound2.xu), 
    min(bound1.yl, bound2.yl), max(bound1.yu, bound2.yu))
  def withinBound(bound: B[T], point: P[T]): Boolean =
    bound.xl <= point.x && bound.xu >= point.x &&
    bound.yl <= point.y && bound.yu >= point.y
}

object NumericOps {
  implicit def apply[T : Numeric] = new NumericOps[T]
}

sealed trait R[T]
case class L[T](point: P[T]) extends R[T]
case class N[T](bound: B[T], left: R[T], right: R[T]) extends R[T]

object RTree {

  def add[T : Ops](tree: R[T], newPoint: P[T]): N[T] = {
    val ops = implicitly[Ops[T]]
    import ops._
    tree match {
      case L(point) =>
        N(initBound(point, newPoint), L(point), L(newPoint))
      case N(bound, left: L[T], right) =>
        val newLeft = add(left, newPoint)
        N(expandBound(bound, newLeft.bound), newLeft, right)
      case N(bound, left: N[T], right) if withinBound(left.bound, newPoint) =>
        val newLeft = add(left, newPoint)
        N(expandBound(bound, newLeft.bound), newLeft, right)
      case N(bound, left, right: N[T]) =>
        val newRight = add(right, newPoint)
        N(expandBound(bound, newRight.bound), left, newRight)
    }
  }

  def find[T](tree: R[T], point: P[T]): Option[P[T]] = tree match {
    case L(p) if p === point => Some(p)
    case L(_) => None
    case N(_, left, right) => find(left, point).orElse(find(right, point))
  }

  def pretty[T](tree: R[T], indent: Int = 0): String = {
    def i = " "*indent
    tree match {
      case l: L[T] =>
        s"$i$l"
      case N(bound, left, right) =>
        s"${i}N(\n$i  $bound\n${pretty(left, indent + 2)}\n${pretty(right, indent + 2)}"
    }
  }

}

object TestRTree {
    import RTree._, NumericOps._
    val i = add(L(P(1,1)), P(2,2))
    val j = add(i, P(2,5))
    val k = add(j, P(1,1))
}
