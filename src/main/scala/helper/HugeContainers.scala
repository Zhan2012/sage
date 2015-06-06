/**
 * Huge Containers
 */
package helper

/**
 * @author ShiZhan
 * GrowingArray:  Array grow as index been accessed
 * MaxArray:      Array consumes most available memory
 * FlatArray:     Array addresses Long
 * LargeBitSet:   BitSet indexed by Long
 */
object HugeContainers {
  class GrowingArray[T: Manifest](v: T) {
    import scala.collection.mutable.ArrayBuffer
    import GrowingArray.Const._
    val data = ArrayBuffer.fill(1)(Array.fill(sizeRow)(v))
    private def more(n: Int) = (1 to n).foreach { i => data += Array.fill(sizeRow)(v) }
    def get(index: Long) = { // 0..21|22..43, index < (1 << 43)
      val high = (index >> scaleRow).toInt
      if (high >= data.size) more(high - data.size + 1)
      val low = (index & maskRow).toInt
      data(high)(low)
    }
    def put(index: Long, d: T) = {
      val high = (index >> scaleRow).toInt
      if (high >= data.size) more(high - data.size + 1)
      val low = (index & maskRow).toInt
      data(high)(low) = d
    }
    def size = data.size.toLong << scaleRow
  }

  object GrowingArray {
    object Const {
      val scaleRow = 21
      val sizeRow = 1 << scaleRow
      val maskRow = sizeRow - 1
    }

    def apply[T: Manifest](v: T) = new GrowingArray[T](v)
  }

  class MaxArray[T: Manifest](v: T) {
    import MaxArray.maxSize
    require(maxSize > 0 && maxSize < Int.MaxValue)
    val data = Array.fill(maxSize.toInt)(v)
    def get(index: Long) = data(index.toInt)
    def put(index: Long, d: T) = data(index.toInt) = d
    def unused(index: Long) = data(index.toInt) == v
    def used = (0 /: data) { (r, d) => if (d != v) r + 1 else r }
    def iterator = data.toIterator.zipWithIndex.filter(_._1 != v).map { case (d, i) => (i.toLong, d) }
    def size = data.length
  }

  object MaxArray {
    import JStates.MEM
    val pow2s = (0 to 62).map(1L << _)
    def nextPow2(l: Long) = pow2s.find(_ > l)
    def prevPow2(l: Long) = pow2s.reverse.find(_ < l)
    val maxSize = prevPow2(MEM.MAX) match { case Some(s) => s >> 3; case _ => -1 }

    def apply[T: Manifest](v: T) = new MaxArray[T](v)
  }

  class FlatArray[T: Manifest](size: Long, default: T) {
    import FlatArray.{ Const, split }
    val (s2, s1, s0) = split(size)
    require(s2 > 0 || s1 > 0 || s0 > 0)
    val d2 = if (s1 == 0 && s0 == 0) s2 else s2 + 1
    val d1 = if (s2 == 0) { if (s0 == 0) s1 else s1 + 1 } else Const.size1
    val d0 = if (s1 == 0 && s2 == 0) s0 else Const.size0
    val data = Array.fill(d2, d1, d0)(default)
    def apply(index: Long) = {
      val (i2, i1, i0) = split(index)
      data(i2)(i1)(i0)
    }
    def update(index: Long, v: T) = {
      val (i2, i1, i0) = split(index)
      data(i2)(i1)(i0) = v
    }
  }

  object FlatArray {
    object Const {
      val scale0 = 22
      val scale1 = 44
      val size0 = 1 << 22
      val size1 = 1 << 22
      val mask0 = (1L << 22) - 1L
      val mask1 = (1L << 22) - 1L
    }

    def split(i: Long) = (
      (i >> Const.scale1).toInt,
      ((i >> Const.scale0) & Const.mask1).toInt,
      (i & Const.mask0).toInt)
    def apply[T: Manifest](size: Long, default: T) = new FlatArray[T](size, default)
  }

  class LargeBitSet {
    import LargeBitSet.Const.{ maskLong, sizeLong }
    import LargeBitSet.countBits
    val data = Array.fill(sizeLong)(0L)
    def get(index: Long) = { (data((index >> 6).toInt) & (1 << (index & maskLong))) != 0 }
    def set(index: Long) = { data((index >> 6).toInt) |= (1 << (index & maskLong)) }
    def clear = { (0 to (sizeLong - 1)).foreach { data.update(_, 0L) } }
    def size = (0 /: data) { (r, d) => r + countBits(d) }
    def isEmpty = data.forall(_ == 0)
  }

  object LargeBitSet {
    object Const {
      val scaleBit = 28
      val scaleLong = scaleBit - 6
      val sizeLong = 1 << scaleLong // 32 MB, 1 << 28 bits.
      val maskLong = (1 << 6) - 1
    }

    def countBits(l: Long) = { var c = 0; var d = l; while (d != 0) { d &= (d - 1); c += 1 }; c }
    def apply() = new LargeBitSet()
  }
}