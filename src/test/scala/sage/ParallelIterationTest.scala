package sage.test

object ParallelIterationTest {
  import java.util.concurrent.ThreadLocalRandom
  import java.security.MessageDigest
  import helper.Timing._

  def someOps = {
    val md = MessageDigest.getInstance("SHA1")
    val bytes = Array.fill(1 << 25)(0.toByte)
    ThreadLocalRandom.current().nextBytes(bytes)
    md.update(bytes)
    md.digest().map("%02x".format(_)).mkString
  }

  val r = 1 to 1 << 3

  val batchOps = Seq(
    ("seq loop", () => for (i <- r) yield someOps),
    ("par loop", () => for (i <- r.par) yield someOps),
    ("par map ", () => r.par.map(_ => someOps)),
    ("par map ", () => (1 to 1 << 4).par.map(_ => someOps)),
    ("par map ", () => (1 to 1 << 5).par.map(_ => someOps)))

  def main(args: Array[String]) = {
    println("        items  elapsed (ms)")
    for ((description, op) <- batchOps) {
      val (r, e) = op.elapsed
      println("%8s% 5d  % 12d".format(description, r.size, e))
    }
  }
}
