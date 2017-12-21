package nl.biopet.utils.tool

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.{DataProvider, Test}
import java.io.File
import java.nio.file.Files

import org.scalatest.Assertions

import scala.io.Source
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
    def argsParser: AbstractOptParser[TestArgs] = new AbstractOptParser[TestArgs](this) {
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

  @DataProvider(name = "release")
  def releaseProvider(): Array[Array[Any]] = {
    Array(Array(false), Array(true))
  }

  @Test(dataProvider = "release")
  def testDocumentation(release: Boolean): Unit = {
    val outputDir = Files.createTempDirectory("test").toFile
    val version = "test_version"
    val versionDir =
      if (release) new File(outputDir, version)
      else new File(outputDir, "develop")
    TestTool.generateDocumentation(outputDir, version, release = release)

    new File(versionDir, "index.md") should exist
    new File(versionDir, "css/docs.css") should exist
    new File(versionDir, "directory.conf") should exist
    new File(versionDir, "default.template.html") should exist

    val index = scala.io.Source.fromFile(versionDir + "/index.md")
    val lines = try index.mkString finally index.close()
    lines should include("--num")
    lines should include("For any question related to TestTool")
    lines should include("TestTool requires Java")
    lines should include("TestTool is part of BIOPET")
    lines should include("# Contact")
    lines should include("# Manual")
    lines should include("# Description")
    lines should include("This is just a test")
    lines should include("comes without a manual.")
    lines should include("for example: a test.")
    lines should include("<td>yes</td>")
    lines should include("<td>no</td>")
    lines should include("<td>yes (2 required)</td>")
    lines should include("<td>yes (unlimited)</td>")
    lines should include("<td>yes (2 times)</td>")
    lines should not include "Should not appear in usage!"
    lines should include("# Contributing")
    lines should include(
      s"""Bug reports, feature requests and feedback can be submitted at our
        |[issue tracker](https://github.com/biopet/testtool/issues).""".stripMargin)
    lines should include("java -jar <TestTool_jar>")

    val configFile = scala.io.Source.fromFile(versionDir + "/directory.conf")
    val configLines = try configFile.mkString finally configFile.close()
    configLines should include("urlToolName = \"testtool\"")
    configLines should include("title = \"TestTool\"")

    if (release) {
      new File(outputDir, "index.html") should exist
      val redirector = Source.fromFile(new File(outputDir, "index.html")).mkString
      redirector should include(s"""window.location.replace("./$version/index.html");""")
      redirector should include(s"""<a href="./$version/index.html">Click here to go to TestTool documentation.""")
    } else {
      new File(outputDir, "index.html") shouldNot exist
    }
  }

  @Test
  def testReadme(): Unit = {
    val readmeFile = new File("target/test/readme/README.md")
    TestTool.generateReadme(readmeFile)
    val readme = scala.io.Source.fromFile(readmeFile)
    val lines = try readme.mkString finally readme.close()

    lines should include("https://biopet.github.io/testtool")
    lines should include("# Documentation")
    lines should include("# TestTool")
    lines should include("TestTool is part of BIOPET")
    lines should include("For any question related to TestTool")

  }

  @Test
  def testExample(): Unit = {
    val example = TestTool.unsafeExample("-a 3 -b 2", "bla 4", "--bla", "--config config.yaml config2.json")
    example should include(
      """    java -jar <TestTool_jar> \
        |    -a 3 \
        |    -b 2 bla 4 \
        |    --bla \
        |    --config config.yaml config2.json""".stripMargin)
    val example2 = TestTool.unsafeExample("this_file.txt -a 3 -b 2", "bla 4", "--bla", "--config config.yaml config2.json")
    example2 should include(
      """    java -jar <TestTool_jar> this_file.txt \
        |    -a 3 \
        |    -b 2 bla 4 \
        |    --bla \
        |    --config config.yaml config2.json""".stripMargin)
    val safeExample = TestTool.example("--sith", "--sith", "-n", "11", "-x")
    safeExample should include(
      """    java -jar <TestTool_jar> \
        |    --sith \
        |    --sith \
        |    -n 11 \
        |    -x""".stripMargin)

    // Following should fail
     intercept[IllegalArgumentException] {
      TestTool.example("--sith", "--sith","--sith", "-n", "11", "-x")
    }
  }

}


