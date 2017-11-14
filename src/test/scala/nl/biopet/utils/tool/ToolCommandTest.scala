package nl.biopet.utils.tool

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test
import java.io.File
class ToolCommandTest extends TestNGSuite with Matchers {

  case class TestArgs(num: Int = 1)

  object TestTool extends ToolCommand[TestArgs] {

    def descriptionText: String = "This is just a test"

    def manualText: String = "This test tool comes without a manual."

    def exampleText: String = "You could use this tool in for example: a test."

    var count = 0

    def main(args: Array[String]): Unit = {
      val cmdArgs = cmdArrayToArgs(args)

      count = cmdArgs.num
    }

    /** This is the parser object that will be tested. */
    def argsParser: AbstractOptParser[TestArgs] = new AbstractOptParser[TestArgs]("test") {
      opt[Int]('n', "num") action { (a, b) => b.copy(num = a) }
    }

    /** Returns an empty/default args case class */
    def emptyArgs: TestArgs = TestArgs()


  }

  @Test
  def test(): Unit = {
    TestTool.count shouldBe 0
    TestTool.main(Array())
    TestTool.toolName shouldBe "TestTool"
    TestTool.count shouldBe 1
    TestTool.main(Array("-n", "11"))
    TestTool.count shouldBe 11
  }

  @Test
  def testDocumentation(): Unit = {
    val outputDir = "target/test/docs/"
    TestTool.generateDocumentation(outputDir)
    new File(outputDir, "index.md") should exist
    new File(outputDir, "css/bootstrap.css") should exist
    new File(outputDir, "css/docs.css") should exist
    new File(outputDir, "directory.conf") should exist
    new File(outputDir, "default.template.html") should exist

    val index = scala.io.Source.fromFile(outputDir + "index.md")
    val lines = try index.mkString finally index.close()
    lines should contain ("--num")
    lines should contain ("For any question related to this tool")
    lines should contain ("This tool requires java")
    lines should contain ("This tool is part of BIOPET")
    lines should contain ("# Contact")
    lines should contain ("# Manual")
    lines should contain ("This is just a test")
    lines should contain ("comes without a manual.")
    lines should contain ("for example: a test.")
  }
}

