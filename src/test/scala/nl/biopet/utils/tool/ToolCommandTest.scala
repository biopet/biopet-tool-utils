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
      opt[Unit]('x', "longX") required()
      opt[Unit]("power") unbounded() text "Palpatine's requested feature"
      opt[Unit]("sith") minOccurs 2 maxOccurs 2 text "There are always 2"
      opt[Unit]("hidden") hidden() text "Should not appear in usage!"
    }

    /** Returns an empty/default args case class */
    def emptyArgs: TestArgs = TestArgs()


  }

  @Test
  def test(): Unit = {
    TestTool.count shouldBe 0
    TestTool.main(Array("-x","--sith", "--sith"))
    TestTool.toolName shouldBe "TestTool"
    TestTool.count shouldBe 1
    TestTool.main(Array("--sith", "--sith", "-n", "11", "-x"))
    TestTool.count shouldBe 11
  }

  @Test
  def testDocumentation(): Unit = {
    val outputDir = new File("target/test/docs/")
    TestTool.generateDocumentation(outputDir)
    new File(outputDir, "/index.md") should exist
    new File(outputDir, "/css/bootstrap.css") should exist
    new File(outputDir, "/css/docs.css") should exist
    new File(outputDir, "/directory.conf") should exist
    new File(outputDir, "/default.template.html") should exist

    val index = scala.io.Source.fromFile(outputDir + "/index.md")
    val lines = try index.mkString finally index.close()
    lines should include("--num")
    lines should include("For any question related to this tool")
    lines should include("This tool requires Java")
    lines should include("This tool is part of BIOPET")
    lines should include("# Contact")
    lines should include("# Manual")
    lines should include("# TestTool")
    lines should include("This is just a test")
    lines should include("comes without a manual.")
    lines should include("for example: a test.")
    lines should include("<td>yes</td>")
    lines should include("<td>no</td>")
    lines should include("<td>yes (2 required)</td>")
    lines should include("<td>yes (unlimited)</td>")
    lines should include("<td>yes (2 times)</td>")
    lines should not include "Should not appear in usage!"
  }
  @Test
  def testReadme(): Unit = {
    val readmeFile = new File("target/test/readme/README.md")
    TestTool.generateReadme(readmeFile)
    val readme = scala.io.Source.fromFile(readmeFile)
    val lines = try readme.mkString finally readme.close()

    lines should include("https://biopet.github.io/testtool")
    lines should include("# Documentation")
    lines should include("This tool is part of BIOPET")
    lines should include("For any question related to this tool")

  }
}


