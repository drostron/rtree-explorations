package ndimrtree

import shapeless._, test._
import spire.implicits._
import NDimRTree._

// TODO : follow shapeless's lead on this style of testing

object Test {

  illTyped("""

  initBox(Point(1 :: HNil), Point("a" :: HNil))

  """)

  val b1 = initBox(Point(1 :: HNil), Point(3 :: HNil))

  val b2 = initBox(Point(2 :: HNil), Point(4 :: HNil))

  val b3 = initBox(Point("a" :: HNil), Point("z" :: HNil))

  val b4 = initBox(Point(1 :: 3 :: HNil), Point(2 :: 4 :: HNil))

  expandBox(b1, b2)

  illTyped("""

  expandBox(b1, b3)

  """)

  illTyped("""

  expandBox(b1, b4)

  """)

  val t1: RTree[String, Int :: Double :: HNil] = RTree(List(Entry("z", Point(3 :: 1.7 :: HNil))))

  illTyped("""

  val t1: RTree[String, Int :: Long :: HNil] = RTree(List(Entry("z", Point(3 :: 1.7 :: HNil))))

  """)

  illTyped("""

  val t1: RTree[List[String], Int :: Double :: HNil] = RTree(List(Entry("z", Point(3 :: 1.7 :: HNil))))

  """)

}
