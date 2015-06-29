package graph

/**
 * @author Zhan
 * graph processor
 * algorithm:     algorithm name and parameters
 * edgeFileName:  input edge list
 */
object Processor {
  import algorithms._
  import graph.{ SimpleEdgeFile, WeightedEdgeFile }
  import helper.Lines.LinesWrapper

  def run(edgeFileName: String, algOpt: String) = {
    implicit lazy val ep = new SimpleEdgeFile(edgeFileName)
    implicit lazy val wep = new WeightedEdgeFile(edgeFileName)

    val algorithm = algOpt.split(":").toList match {
      case "bfs" :: root :: Nil => new BFS(root.toLong)
      case "bfs" :: "u" :: root :: Nil => new BFS_U(root.toLong)
      case "sssp" :: root :: Nil => new SSSP(root.toLong)
      case "sssp" :: "u" :: root :: Nil => new SSSP_U(root.toLong)
      case "cc" :: Nil => new CC
      case "kcore" :: Nil => new KCore
      case "pagerank" :: nLoop :: Nil => new PageRank(nLoop.toInt)
      case "degree" :: Nil => new Degree
      case "degree" :: "u" :: Nil => new Degree_U
      case _ => new Degree_U
    }

    val result = algorithm.run
    ep.close
    wep.close
    val outFileName = edgeFileName + "-" + algOpt.replace(':', '-') + ".out"
    if (!result.isEmpty)
      result.map { case (k: Long, v: Any) => s"$k $v" }.toFile(outFileName)
  }
}
