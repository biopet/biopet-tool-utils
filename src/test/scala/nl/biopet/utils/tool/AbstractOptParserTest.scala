package nl.biopet.utils.tool

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class AbstractOptParserTest extends BiopetTest {
  @Test
  def test(): Unit = {
    val args: Array[String] = Array()
    case class Args()
    class ArgsParser(cmdName: String) extends AbstractOptParser[Args](cmdName)
    val parser = new ArgsParser("name")
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)
  }
}
