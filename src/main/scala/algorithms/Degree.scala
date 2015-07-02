package algorithms

import graph.{ Edge, EdgeProvider, SimpleEdge }
import helper.IteratorOps.VisualOperations

case class DirectedDegree(i: Int, o: Int) {
  def addIDeg = DirectedDegree(i + 1, o)
  def addODeg = DirectedDegree(i, o + 1)
  override def toString = s"$i $o"
}

class Degree(implicit ep: EdgeProvider[SimpleEdge]) extends Algorithm[DirectedDegree] {
  val default = DirectedDegree(0, 0)

  def iterations() = {
    logger.info("Counting vertex in and out degree ...")
    ep.getEdges.foreachDo {
      case Edge(u, v) =>
        data(u) = data.getOrElse(u, default).addODeg
        data(v) = data.getOrElse(u, default).addIDeg
    }
  }
}

class Degree_U(implicit ep: EdgeProvider[SimpleEdge]) extends Algorithm[Long] {
  def iterations() = {
    logger.info("Counting vertex degree ...")
    ep.getEdges.foreachDo {
      case Edge(u, v) =>
        data(u) = data.getOrElse(u, 0L) + 1
        data(v) = data.getOrElse(v, 0L) + 1
    }
  }
}