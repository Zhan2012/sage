package configuration

/**
 * @author ShiZhan
 * parsing program options and prepare default configuration
 *
 * NOTE:
 * if environment variables are not defined/exported, current directory will be used.
 */
object Options {
  import java.io.File
  import scala.util.Properties.{ envOrElse, userDir }

  val pwd = userDir
  private val tempOrPwdTemp = new File(envOrElse("TEMP", pwd + "/temp"))
  if (!tempOrPwdTemp.exists) tempOrPwdTemp.mkdir
  val tempDirDefault = tempOrPwdTemp.getAbsolutePath

  type OptionMap = Map[Symbol, Any]

  class OptionMapWrapper(om: OptionMap) {
    def isEmpty = om.isEmpty
    def getBool(s: Symbol) = om.contains(s)
    def getString(s: Symbol, d: String) = om.getOrElse(s, d).asInstanceOf[String]
    def getInt(s: Symbol, d: Int) = om.getOrElse(s, d).asInstanceOf[Int]
    def getSpecifiedInt(s: Symbol, checker: Int => Boolean, d: Int) = om.get(s) match {
      case Some(value) if value.isInstanceOf[Int] =>
        val i = value.asInstanceOf[Int]
        if (checker(i)) i else d
      case _ => d
    }
  }

  private def isSwitch(s: String) = s.startsWith("-")
  private def nextOption(map: OptionMap, optList: List[String]): OptionMap = {
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
      case "--binary" :: more =>
        nextOption(map ++ Map('binary -> true), more)
      case "--self-loop" :: more =>
        nextOption(map ++ Map('selfloop -> true), more)
      case "--bidirectional" :: more =>
        nextOption(map ++ Map('bidirectional -> true), more)
      case "--sort" :: more =>
        nextOption(map ++ Map('sort -> true), more)
      case "--uniq" :: more =>
        nextOption(map ++ Map('uniq -> true), more)
      case "--vdb" :: vdbFile :: more =>
        nextOption(map ++ Map('vdbfile -> vdbFile), more)
      case "--out" :: outFile :: more =>
        nextOption(map ++ Map('outfile -> outFile), more)
      case "--temp" :: tempDir :: more =>
        nextOption(map ++ Map('tempDir -> tempDir), more)
      case inFile :: opt :: more if isSwitch(opt) =>
        nextOption(map ++ Map('infile -> inFile), optList.tail)
      case inFile :: Nil => map ++ Map('infile -> inFile)
      case _ => map
    }
  }

  def getOptions(optList: List[String]) = {
    val options = nextOption(Map(), optList)
    new OptionMapWrapper(options)
  }
}