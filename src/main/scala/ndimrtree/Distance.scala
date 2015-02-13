package ndimrtree

import shapeless._

trait Distance[T <: HList] {
  def distance(a: Point[T], b: Point[T]): Double
}
