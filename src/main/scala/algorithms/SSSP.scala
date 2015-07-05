package algorithms

/**
 * Single Source Shortest Path
 * SSSP:   working on directed weighted graphs
 * SSSP_U: working on undirected weighted graphs
 */
import graph.{ Edge, WeightedEdge, EdgeProvider }

class SSSP(root: Int)(implicit ep: EdgeProvider[WeightedEdge]) extends Algorithm[Float] {
  def iterations() = {
    scatter(root, 0.0f)
    update
    while (!gather.isEmpty) {
      for (Edge(u, v, w) <- ep.getEdges if gather(u)) {
        val distance = data(u) + w
        val target = data.getOrElse(v, Float.MaxValue)
        if (target > distance) scatter(v, distance)
      }
      update
    }
  }
}

class SSSP_U(root: Int)(implicit ep: EdgeProvider[WeightedEdge]) extends Algorithm[Float] {
  def iterations() = {
    scatter(root, 0.0f)
    update
    while (!gather.isEmpty) {
      for (Edge(u, v, w) <- ep.getEdges) {
        if (gather(u)) {
          val distance = data(u) + w
          val target = data.getOrElse(v, Float.MaxValue)
          if (target > distance) scatter(v, distance)
        }
        if (gather(v)) {
          val distance = data(v) + w
          val target = data.getOrElse(u, Float.MaxValue)
          if (target > distance) scatter(u, distance)
        }
      }
      update
    }
  }
}
