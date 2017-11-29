package nl.biopet.utils.tool

import nl.biopet.test.BiopetTest
import org.apache.log4j.Level
import org.testng.annotations.Test

class AbstractOptParserTest extends BiopetTest {

  case class TestArgs()

  object EmptyTool extends ToolCommand[TestArgs] {

    def descriptionText: String = ""
    def manualText: String = ""
    def exampleText: String = ""
    def main(args: Array[String]): Unit = {
      val cmdArgs = cmdArrayToArgs(args)
    }

    /** This is the parser object that will be tested. */
    def argsParser: AbstractOptParser[TestArgs] = new AbstractOptParser[TestArgs]("test") {
    }

    /** Returns an empty/default args case class */
    def emptyArgs: TestArgs = TestArgs()


  }
  @Test
  def testParse(): Unit = {
    val args: Array[String] = Array()
    case class Args()
    class ArgsParser(cmdName: String) extends AbstractOptParser[Args](cmdName)
    val parser = new ArgsParser("name")
    val cmdArgs =
      parser.parse(args, Args()).getOrElse(throw new IllegalArgumentException)
  }

  @Test
  def testVerbosityFlags(): Unit = {

    EmptyTool.main(Array("--log_level", "debug"))
    EmptyTool.logger.getLevel shouldBe Level.DEBUG
    EmptyTool.main(Array("--log_level", "info"))
    EmptyTool.logger.getLevel shouldBe Level.INFO
    EmptyTool.main(Array("--log_level", "warn"))
    EmptyTool.logger.getLevel shouldBe Level.WARN
    EmptyTool.main(Array("--log_level", "error"))
    EmptyTool.logger.getLevel shouldBe Level.ERROR
    a [java.lang.IllegalArgumentException] should be thrownBy {
      EmptyTool.main(Array("--log_level", "whatever"))
    }

  }
}
