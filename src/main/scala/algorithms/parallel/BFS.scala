package algorithms.parallel

/*
 * BFS:    BFS on directed graphs and parallel edge lists
 * BFS_U:  BFS on undirected graphs and parallel edge lists
 */
import graph.{ Edge, EdgeProvider, SimpleEdge }

class BFS(root: Int)(implicit eps: Seq[EdgeProvider[SimpleEdge]]) extends Algorithm[Int] {
  def iterations() = {
    var level = 1
    scatter(root, level)
    update

    while (!gather.isEmpty) {
      level += 1
      for (ep <- eps.par; Edge(u, v) <- ep.getEdges if (gather(u) && !data.contains(v))) {
        scatter(v, level)
      }
      update
    }
  }
}

class BFS_U(root: Int)(implicit eps: Seq[EdgeProvider[SimpleEdge]]) extends Algorithm[Int] {
  def iterations() = {
    var level = 1
    scatter(root, level)
    update

    while (!gather.isEmpty) {
      level += 1
      for (ep <- eps.par; Edge(u, v) <- ep.getEdges) {
        if (gather(u) && !data.contains(v)) scatter(v, level)
        if (gather(v) && !data.contains(u)) scatter(u, level)
      }
      update
    }
  }
}