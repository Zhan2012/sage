/**
 * Output Synthetic Graph as edge list
 */
package graph

/**
 * @author Zhan
 * Synthetic Graph Generator
 * RMAT, ER
 */
object Generator {
  import EdgeUtils.EdgesWriter

  implicit class AlmostUniqIterator[T](iterator: Iterator[T]) {
    import scala.collection.mutable.Set
    val sSize = 1024 * 1024 // 16 MB each
    val aSize = 16 // 256 MB total, a 16 M items window for checking duplicates
    val setArray = Array.fill(aSize)(Set[T]())
    var current = 0
    def put(elem: T) = {
      if (setArray(current).size > sSize) {
        current = (current + 1) % aSize
        setArray(current).clear()
      }
      setArray(current).add(elem)
    }
    def contain(elem: T) = !setArray.par.forall { !_.contains(elem) }
    def almostUniq = iterator.map { elem =>
      if (contain(elem))
        None
      else {
        put(elem)
        Some(elem)
      }
    }.filter(_ != None).map(_.get)
  }

  implicit class EdgeMirroring(edges: Iterator[Edge]) {
    def toBidirection = edges.flatMap { e => Seq(e, Edge(e.v, e.u)).toIterator }
  }

  def run(generator: String, outFile: String,
    selfloop: Boolean, bidirection: Boolean, uniq: Boolean) = {
    val edges0 = generator.split(":").toList match {
      case "rmat" :: scale :: degree :: Nil =>
        new RecursiveMAT(scale.toInt, degree.toInt).getIterator
      case "er" :: scale :: ratio :: Nil =>
        new ErdosRenyi(scale.toInt, ratio.toDouble).getIterator
      case "sw" :: scale :: neighbhour :: rewiring :: Nil =>
        new SmallWorld(scale.toInt, neighbhour.toInt, rewiring.toDouble).getIterator
      case _ =>
        println(s"Unknown generator: [$generator]"); Iterator[Edge]()
    }

    val edgesL = if (selfloop) edges0 else edges0.filterNot(_.selfloop)
    val edgesB = if (bidirection) edgesL.toBidirection else edgesL
    val edgesU = if (uniq) edgesB.almostUniq else edgesB
    edgesU.toFile(outFile)
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
    val dices = Array.fill(scale)(Random.nextInt(100)).map(dice)
    val (u, v) = ((0L, 0L) /: dices) { (p0, p1) =>
      val (x0, y0) = p0; val (x1, y1) = p1; ((x0 << 1) + x1, (y0 << 1) + y1)
    }
    if (nEdges <= totalEdges) Edge(u, v) else EdgeUtils.invalidEdge
  }

  def getIterator = Iterator.continually(nextEdge).takeWhile(_.valid)
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