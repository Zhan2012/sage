/**
 * Output Synthetic Graph as edge list
 */
package graph

/**
 * @author Zhan
 * Synthetic Graph Generator
 * RMAT, ER, SW, BA
 */
object Generator {
  import EdgeUtils.EdgesWriter

  def run(generator: String, outFile: String) = {
    val edges = generator.split(":").toList match {
      case "rmat" :: scale :: degree :: Nil =>
        new RecursiveMAT(scale.toInt, degree.toInt).getIterator
      case "er" :: scale :: ratio :: Nil =>
        new ErdosRenyi(scale.toInt, ratio.toDouble).getIterator
      case "sw" :: scale :: neighbhour :: rewiring :: Nil =>
        new SmallWorld(scale.toInt, neighbhour.toInt, rewiring.toDouble).getIterator
      case "ba" :: scale :: m0 :: Nil =>
        new BarabasiAlbert(scale.toInt, m0.toInt).getIterator
      case _ =>
        println(s"Unknown generator: [$generator]"); Iterator[Edge]()
    }
    edges.toFile(outFile)
  }
}

class RecursiveMAT(scale: Int, degree: Long) {
  require(scale > 0 && scale < 32 && degree > 0)
  import scala.util.Random

  val totalEdges = (1L << scale) * degree
  var nEdges = 0L

  def dice(d: Int) =
    if (d < 57) (0, 0) else if (d < 76) (1, 0) else if (d < 95) (0, 1) else (1, 1)

  def nextEdge = {
    nEdges += 1
    val dices = Seq.fill(scale)(Random.nextInt(100)).map(dice)
    val (u, v) = ((0L, 0L) /: dices) { (p0, p1) =>
      val (x0, y0) = p0; val (x1, y1) = p1; ((x0 << 1) + x1, (y0 << 1) + y1)
    }
    Edge(u, v)
  }

  def getIterator = Iterator.continually(nextEdge).takeWhile(_ => nEdges <= totalEdges)
}

class ErdosRenyi(scale: Int, ratio: Double) {
  require(ratio < 1 && ratio > 0)
  import scala.util.Random

  val vTotal = 1L << scale
  val rangeI = 256
  val ratioI = (rangeI * ratio).toInt

  def vertices = {
    var vID = -1L
    Iterator.continually { vID += 1; vID }.takeWhile(_ < vTotal)
  }

  def getIterator = for (u <- vertices; v <- vertices if Random.nextInt(rangeI) < ratioI)
    yield Edge(u, v)
}

class SmallWorld(scale: Int, neighbour: Int, rewiring: Double) {
  require(scale > 0 && neighbour > 0 && rewiring < 1 && rewiring > 0)
  import scala.util.Random

  val vTotal = 1L << scale
  val rangeI = 256
  val rewirI = (rangeI * rewiring).toInt

  def vertices = {
    var vID = -1L
    Iterator.continually { vID += 1; vID }.takeWhile(_ < vTotal)
  }

  def rewire(id: Long) = if (Random.nextInt(rangeI) < rewirI)
    (id + Random.nextInt) & (vTotal - 1)
  else
    id & (vTotal - 1)

  def neighbours(id: Long) = (1 to neighbour).map(id + _).map(rewire).toIterator

  def getIterator = for (u <- vertices; v <- neighbours(u))
    yield Edge(u, v)
}

class BarabasiAlbert(scale: Int, m0: Int) {
  require(scale > 0 && scale < 23 && m0 > 0) // if no '-J-Xmx?g' specified, then 'scale < 25'
  import scala.util.Random
  import scala.collection.mutable.Set

  val vTotal = 1 << scale
  val degree = Array.fill(vTotal)(0) // 2^22 * 4 Bytes = 16MB

  def vertices(total: Int) = {
    var vID = -1
    Iterator.continually { vID += 1; vID }.take(total)
  }

  def neighbours(id: Int) =
    if (id < m0)
      Iterator[Edge]()
    else if (id == m0) {
      degree(id) = m0
      (0 to (m0 - 1)).map { i => degree(i) = 1; Edge(m0, i) }.toIterator
    } else {
      val found = Set[Int]()
      val range0 = (id - m0).toLong * m0 * 2
      for (i <- 1 to m0) {
        val seeds = vertices(id).filterNot(found.contains)
        val range = range0 - (0 /: found.toIterator.map(degree)) { _ + _ }
        var dice = (Random.nextLong.abs % range) + 1
        for (v <- seeds if dice > 0) {
          dice -= degree(v)
          if (dice <= 0) found.add(v)
        }
      }
      degree(id) = m0
      found.toIterator.map { n => degree(n) += 1; Edge(id, n) }
    }

  def getIterator = vertices(vTotal).flatMap(neighbours)
}
