package ndimrtree

import NDimRTree._, NDimRTreeOps._
import scalaz.{ Order => _, _ }, Scalaz._
import shapeless._, ops.hlist._
import spire._, algebra._, implicits._

// TODO:
// - add scalameter, compare to? Archery uses Thyme.
//   Might be interesting to compare NDimHList version to the Archery's 2-dim version.
// - add some property based testing, give spec2 a whirl if something beyond scalacheck is required
// - up the number of checked warts
// - move spark to main src since 2.11 is now available
// - should V type param in Leaf be omitted and leaf V in Entry existential?
// - address imports so there is a more singular import available
//   ah, spire imports required, hmm, wonder if there's a more intuitive way to include
//   those transitively
// - RTree's V, value type, could be a shapeless Coproduct?
// - would be fun to see what algebraic laws apply to RTrees
// - make sure to say I welcome suggestions comments feedback...
// - leverage propensive's heteroargs.scala gist to add sugar over the various HList parameter constructors
// - could just use spire's Intervals and Interval.point?, anything for box?
// - look into typelevel/discipline for law checking

// algebra
// Associative  : If ⊕ is associative, then a ⊕ (b ⊕ c) = (a ⊕ b) ⊕ c).
// Commutative  : If ⊕ is commutative, then a ⊕ b = b ⊕ a.
// Identity     : If id is an identity for ⊕, then a ⊕ id = id ⊕ a = a.
// Inverse	    : If ¬ is an inverse for ⊕ and id, then a ⊕ ¬a = ¬a ⊕ a = id.
// Distributive : If ⊕ and ⊙ distribute, then a ⊙ (b ⊕ c) = (a ⊙ b) ⊕ (a ⊙ c) and (a ⊕ b) ⊙ c = (a ⊙ c) ⊕ (b ⊙ c).
// Idempotent   : If ⊕ is idempotent, then a ⊕ a = a

case class Point[T <: HList](terms: T)
case class Entry[V, T <: HList](value: V, point: Point[T])
case class Box[T <: HList](lowerBounds: T, upperBounds: T) // inclusive

// TODO : Empty not quite there, refine

sealed trait RTree[V, T <: HList]
case class Empty[V, T <: HList]() extends RTree[V, T]
case class Leaf[V, T <: HList](entry: Entry[V, T]) extends RTree[V, T]
case class Node[V, T <: HList](box: Box[T], left: RTree[V, T], right: RTree[V, T]) extends RTree[V, T]

object RTree {
  def apply
    [V, T <: HList : ZWMin : ZWMax, L <: HList : ZWLB[T]#λ : LFA, U <: HList : ZWUB[T]#λ : LFA]
    (entries: List[Entry[V, T]])
    : RTree[V, T] =
    entries.foldLeft[RTree[V, T]](Empty[V, T]())(_ add _)
}

object Point {
  implicit def equalInstance[T <: HList] = Equal.equalA[Point[T]]
}

object Entry {
  implicit def equalInstance[V, T <: HList] = Equal.equalA[Entry[V, T]]
}

object NDimRTree {

  object minimum extends Poly2 {
    implicit def default[T : Order] = at[T, T](implicitly[Order[T]].min)
  }

  object maximum extends Poly2 {
    implicit def default[T : Order] = at[T, T](implicitly[Order[T]].max)
  }

  object lte extends Poly2 {
    implicit def caseOrder[T : Order] = at[T, T](_ <= _)
  }

  object gte extends Poly2 {
    implicit def caseOrder[T : Order] = at[T, T](_ >= _)
  }

  object and extends Poly2 {
    implicit def caseBoolean = at[Boolean, Boolean](_ && _)
  }

  type ZWMin[T <: HList] = ZipWith.Aux[T, T, minimum.type, T]
  type ZWMax[T <: HList] = ZipWith.Aux[T, T, maximum.type, T]

  type ZWLB[T <: HList] = { type λ[U <: HList] = ZipWith.Aux[T, T, lte.type, U] }
  type ZWUB[T <: HList] = { type λ[U <: HList] = ZipWith.Aux[T, T, gte.type, U] }

  type LFLB[T <: HList] =
    { type λ[U <: HList] = LeftFolder.Aux[ZipWith.Aux[T, T, lte.type, U]#Out, Boolean, and.type, Boolean] }
  type LFUB[T <: HList] =
    { type λ[U <: HList] = LeftFolder.Aux[ZipWith.Aux[T, T, gte.type, U]#Out, Boolean, and.type, Boolean] }

  type LFA[T <: HList] = LeftFolder.Aux[T, Boolean, and.type, Boolean]

  def initBox[T <: HList : ZWMin : ZWMax](point1: Point[T], point2: Point[T]): Box[T] = Box(
    point1.terms.zipWith(point2.terms)(minimum),
    point1.terms.zipWith(point2.terms)(maximum))

  def expandBox[T <: HList : ZWMin : ZWMax](box: Box[T], point: Point[T]): Box[T] = Box(
    box.lowerBounds.zipWith(point.terms)(minimum),
    box.upperBounds.zipWith(point.terms)(maximum))

  def expandBox[T <: HList : ZWMin : ZWMax](box1: Box[T], box2: Box[T]): Box[T] = Box(
    box1.lowerBounds.zipWith(box2.lowerBounds)(minimum),
    box1.upperBounds.zipWith(box2.upperBounds)(maximum))

  def withinBox
    [T <: HList, L <: HList : ZWLB[T]#λ : LFLB[T]#λ, U <: HList : ZWUB[T]#λ : LFUB[T]#λ]
    (box: Box[T], point: Point[T])
    : Boolean =
    box.lowerBounds.zipWith(point.terms)(lte).foldLeft(true)(and) &&
    box.upperBounds.zipWith(point.terms)(gte).foldLeft(true)(and)

  def overlaps
    [T <: HList, L <: HList : ZWLB[T]#λ : LFLB[T]#λ, U <: HList : ZWUB[T]#λ : LFUB[T]#λ]
    (box1: Box[T], box2: Box[T])
    : Boolean =
    box1.lowerBounds.zipWith(box2.upperBounds)(lte).foldLeft(true)(and) &&
    box1.upperBounds.zipWith(box2.lowerBounds)(gte).foldLeft(true)(and)

}
