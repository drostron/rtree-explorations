package ndimrtree

import scalaz._, Scalaz._
import shapeless._
import spire.math._
import NDimRTree._, NDimRTreeOps._

// TODO : crisp up, not quite ideal yet,
// should ops be monomorphic on rtree or have polymorphic dispatch like it is currently

object NDimRTreeOps {
  implicit def toLeafOps
    [V, T <: HList : ZWMin : ZWMax]
    (leaf: Leaf[V, T])
    : NDimLeafOps[V, T] =
    new NDimLeafOps(leaf)

  implicit def toNodeOps
    [V, T <: HList : ZWMin : ZWMax, L <: HList : ZWLB[T]#λ : LFA, U <: HList : ZWUB[T]#λ : LFA]
    (node: Node[V, T])
    : NDimNodeOps[V, T, L, U] =
    new NDimNodeOps(node)

  implicit def toRTreeOps
    [V, T <: HList : ZWMin : ZWMax, L <: HList : ZWLB[T]#λ : LFA, U <: HList : ZWUB[T]#λ : LFA]
    (rtree: RTree[V, T])
    : NDimRTreeOps[V, T, L, U] =
    new NDimRTreeOps(rtree)

  implicit def toPointOps
    [T <: HList : Distance]
    (point: Point[T])
    : PointOps[T] =
    new PointOps(point)
}

class NDimLeafOps
  [V, T <: HList : ZWMin : ZWMax]
  (leaf: Leaf[V, T]) {

  def add(entry: Entry[V, T]): Node[V, T] =
    Node(initBox(leaf.entry.point, entry.point), leaf, Leaf(entry))
}

class NDimNodeOps
  [V, T <: HList : ZWMin : ZWMax, L <: HList : ZWLB[T]#λ : LFA, U <: HList : ZWUB[T]#λ : LFA]
  (node: Node[V, T]) {

  def add(entry: Entry[V, T]): Node[V, T] =
    node match {
    case Node(box, _: Empty[V, T], right) =>
      Node(expandBox(box, entry.point), Leaf(entry), right)
    case Node(box, left, _: Empty[V, T]) =>
      Node(expandBox(box, entry.point), left, Leaf(entry))
    case Node(box, left: Leaf[V, T], right) =>
      val newLeft = left.add(entry)
      Node(expandBox(box, newLeft.box), newLeft, right)
    case Node(box, left: Node[V, T], right) if withinBox(left.box, entry.point) =>
      val newLeft = left.add(entry)
      Node(expandBox(box, newLeft.box), newLeft, right)
    case Node(box, left, right: Leaf[V, T]) =>
      val newRight = right.add(entry)
      Node(expandBox(box, newRight.box), left, newRight)
    case Node(box, left, right: Node[V, T]) =>
      val newRight = right.add(entry)
      Node(expandBox(box, newRight.box), left, newRight)
    }

}

class NDimRTreeOps
  [V, T <: HList : ZWMin : ZWMax, L <: HList : ZWLB[T]#λ : LFA, U <: HList : ZWUB[T]#λ : LFA]
  (rtree: RTree[V, T]) {

  // TODO : a bit unruly; what exactly did I mean by unruly?
  def add(entry: Entry[V, T]): RTree[V, T] =
    rtree match {
    case _: Empty[V, T] => Leaf[V, T](entry)
    case leaf: Leaf[V, T] => leaf.add(entry)
    case node: Node[V, T] => node.add(entry)
    }

  // TODO : handle box shrinking
  // archery throws an error if the entry was not present
  def remove(entry: Entry[V, T]): RTree[V, T] =
    rtree match {
    case e: Empty[V, T] => e
    case leaf: Leaf[V, T] if leaf.entry === entry => Empty[V, T]()
    case leaf: Leaf[V, T] => leaf
    case Node(box, left, right) =>
      Node(box, left.remove(entry), right.remove(entry))
    }

  def find(point: Point[T])
    : Option[Entry[V, T]] =
    rtree match {
    case _: Empty[V, T] => None
    case leaf: Leaf[V, T] => (leaf.entry.point === point).option(leaf.entry)
    case node: Node[V, T] => node.left.find(point).orElse(node.right.find(point))
    }

  def contains(entry: Entry[V, T]): Boolean = find(entry.point).isDefined

  def search(space: Box[T]): Vector[Entry[V, T]] = search(space, _ => true)

  def search(space: Box[T], f: Entry[V, T] => Boolean): Vector[Entry[V, T]] =
    rtree match {
    case leaf: Leaf[V, T] if withinBox(space, leaf.entry.point) && f(leaf.entry) =>
      Vector(leaf.entry)
    case node: Node[V, T] if overlaps(space, node.box) =>
      node.left.search(space) ++ node.right.search(space)
    case _ => Vector.empty
    }

  // TODO : incorrect
  def nearest(point: Point[T])
    : Option[Entry[V, T]] =
    rtree match {
    case _: Empty[V, T] => None
    case leaf: Leaf[V, T] => (leaf.entry.point === point).option(leaf.entry)
    case node: Node[V, T] => node.left.find(point).orElse(node.right.find(point))
    }

  def count(space: Box[T]): Int = search(space).size

  lazy val entries: Vector[Entry[V, T]] =
    rtree match {
    case _: Empty[_, _] => Vector.empty
    case Leaf(entry) => Vector(entry)
    case Node(_, left, right) => left.entries ++ right.entries
    }

  lazy val pretty: String = pretty(0)

  def pretty(indent: Int): String = {
    def i = " "*indent
    rtree match {
    case e: Empty[V, T] => s"$i$e"
    case l: Leaf[V, T] =>
      s"$i$l"
    case Node(box, left, right) =>
      s"${i}Node(\n$i  $box,\n${left.pretty(indent + 2)},\n${right.pretty(indent + 2)}"
    }
  }

  lazy val leafDepths: Vector[Number] = leafDepths(0)

  def leafDepths(depth: Number): Vector[Number] =
    rtree match {
    case _: Empty[V, T] => Vector.empty
    case l: Leaf[V, T] => Vector(depth)
    case Node(box, left, right) =>
      left.leafDepths(depth + 1) ++ right.leafDepths(depth + 1)
    }

}

class PointOps[T <: HList : Distance](a: Point[T]) {
  def distance(b: Point[T]): Double = implicitly[Distance[T]].distance(a, b)
}
