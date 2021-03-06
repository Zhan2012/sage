package graph.algorithms

/**
 * Single Source Shortest Path
 * SSSP:   working on directed weighted graphs
 * SSSP_U: working on undirected weighted graphs
 */
import graph.{ Edge, WeightedEdge }
import graph.Parallel.Algorithm
import helper.GrowingArray

class SSSP(root: Long) extends Algorithm[WeightedEdge, Double] {
  val distance = GrowingArray[Double](Double.MaxValue)
  distance(root) = 0.0f
  gather.add(root)

  def compute(edges: Iterator[WeightedEdge]) =
    for (Edge(u, v, w) <- edges if (gather(u)))
      distance.synchronized {
        val d = distance(u) + w
        if (distance(v) > d) {
          distance(v) = d
          scatter.add(v)
        }
      }

  def update() = {}
  def complete() = distance.updated
}

class SSSP_U(root: Long) extends Algorithm[WeightedEdge, Double] {
  val distance = GrowingArray[Double](Double.MaxValue)
  distance(root) = 0.0f
  gather.add(root)

  def compute(edges: Iterator[WeightedEdge]) =
    for (Edge(u, v, w) <- edges) distance.synchronized {
      if (gather(u)) {
        val d = distance(u) + w
        if (distance(v) > d) {
          distance(v) = d
          scatter.add(v)
        }
      }
      if (gather(v)) {
        val d = distance(v) + w
        if (distance(u) > d) {
          distance(u) = d
          scatter.add(u)
        }
      }
    }

  def update() = {}
  def complete() = distance.updated
}
