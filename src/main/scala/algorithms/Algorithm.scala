package algorithms

case class Context(prefix: String, nShard: Int, verticesDB: String)

abstract class Algorithm[Value](context: Context)
    extends helper.Logging {
  import scala.collection.JavaConversions._
  import graph.{ Vertices, Shards, DirectionalShards }

  val Context(prefix, nShard, verticesDB) = context
  val shards: Shards
  val vdb = new Vertices[Value](verticesDB)

  var stepCounter = 0
  def step(i: Int) = vdb.getVertexTable(s"$i")
  val data = step(0)
  def gather = step(stepCounter)
  def scatter = step(stepCounter + 1)
  def update = {
    val flags = shards match { case s: DirectionalShards => s.getFlagState; case _ => "" }
    val stat = "[ % 10d -> % 10d ] %s".format(gather.size, scatter.size, flags)
    gather.clear()
    data.putAll(scatter)
    stepCounter += 1
    logger.info("Step {}: {}", stepCounter, stat)
  }

  def iterations: Unit

  def run =
    if (shards.intact) {
      logger.info("Data:      [{}]", prefix)
      logger.info("Sharding:  [{}]", nShard)
      logger.info("Vertex DB: [{}]", verticesDB)
      iterations
      if (data.isEmpty())
        None
      else {
        logger.info("Generating results ...")
        val result = data.toIterator
        Some(result)
      }
    } else {
      logger.info("edge list(s) incomplete")
      None
    }
}

abstract class SimpleAlgorithm[T](context: Context) extends Algorithm[T](context: Context) {
  val shards = new graph.SimpleShards(context.prefix, context.nShard)
}

abstract class DirectionalAlgorithm[T](context: Context) extends Algorithm[T](context: Context) {
  val shards = new graph.DirectionalShards(context.prefix, context.nShard)
}

abstract class BidirectionalAlgorithm[T](context: Context) extends Algorithm[T](context: Context) {
  val shards = new graph.BidirectionalShards(context.prefix, context.nShard)
}