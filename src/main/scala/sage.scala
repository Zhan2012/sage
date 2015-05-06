/**
 * @author Zhan.Shi
 * @year 2015
 * @name SAGE Project
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */
object sage {
  import graph.{ Importer, Processor, Remapper, Generator }
  import graph.Shards.nShardShouldBePowerOf2
  import helper.Resource

  lazy val usage = Resource.getString("functions.txt")

  type OptionMap = Map[Symbol, Any]

  def isSwitch(s: String) = s.startsWith("-")

  def nextOption(map: OptionMap, optList: List[String]): OptionMap = {
    optList match {
      case "-h" :: more =>
        nextOption(map ++ Map('help -> true), more)
      case "-i" :: more =>
        nextOption(map ++ Map('import -> true), more)
      case "-p" :: algorithm :: more =>
        nextOption(map ++ Map('process -> algorithm), more)
      case "-m" :: mapfile :: more =>
        nextOption(map ++ Map('remap -> mapfile), more)
      case "-g" :: generator :: more =>
        nextOption(map ++ Map('generate -> generator), more)
      case "--shard" :: value :: more =>
        nextOption(map ++ Map('nShard -> value.toInt), more)
      case "--self-loop" :: more =>
        nextOption(map ++ Map('selfloop -> true), more)
      case "--bidirection" :: more =>
        nextOption(map ++ Map('bidirection -> true), more)
      case "--uniq" :: more =>
        nextOption(map ++ Map('uniq -> true), more)
      case "--output" :: outFile :: more =>
        nextOption(map ++ Map('outfile -> outFile), more)
      case inFile :: opt :: more if isSwitch(opt) =>
        nextOption(map ++ Map('infile -> inFile), optList.tail)
      case inFile :: Nil => map ++ Map('infile -> inFile)
      case _ => map
    }
  }

  def main(args: Array[String]) = {
    val options = nextOption(Map(), args.toList)
    if (options.isEmpty) println(usage)
    else {
      val inFile = options.getOrElse('infile, "").asInstanceOf[String]
      val outFile = options.getOrElse('outfile, "").asInstanceOf[String]
      val mapFile = options.getOrElse('remap, "").asInstanceOf[String]
      val nShard = options.getOrElse('nShard, 1).asInstanceOf[Int].toPowerOf2
      val algorithm = options.getOrElse('process, "").asInstanceOf[String]
      val generator = options.getOrElse('generate, "").asInstanceOf[String]
      val selfloop = options.contains('selfloop)
      val bidirection = options.contains('bidirection)
      val uniq = options.contains('uniq)
      if (options.contains('help)) println(usage)
      else if (options.contains('import)) Importer.run(inFile, nShard)
      else if (options.contains('process)) Processor.run(inFile, nShard, algorithm)
      else if (options.contains('remap)) Remapper.run(inFile, mapFile, outFile)
      else if (options.contains('generate)) Generator.run(generator, outFile, selfloop, bidirection, uniq)
    }
  }
}
