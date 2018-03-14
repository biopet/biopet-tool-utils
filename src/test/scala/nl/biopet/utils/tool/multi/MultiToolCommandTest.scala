package nl.biopet.utils.tool.multi

import nl.biopet.test.BiopetTest
import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}
import org.testng.annotations.Test

class MultiToolCommandTest extends BiopetTest {
  case class TestArgs()

  object TestTool extends ToolCommand[TestArgs] {

    override def toolName = "TestTool"

    def descriptionText: String = "This is just a test"

    def manualText: String = "This test tool comes without a manual."

    def exampleText: String = "You could use this tool in for example: a test."

    var count = 0

    def main(args: Array[String]): Unit = {
      val cmdArgs = cmdArrayToArgs(args)
      count += 1
    }

    /** This is the parser object that will be tested. */
    def argsParser: AbstractOptParser[TestArgs] =
      new AbstractOptParser[TestArgs](this) {}

    /** Returns an empty/default args case class */
    def emptyArgs: TestArgs = TestArgs()
  }

  object MultiTestTool1 extends MultiToolCommand {
    def subTools: Map[String, List[ToolCommand[_]]] =
      Map("mode" -> List(TestTool))

    /** Force description to be written for each tool. */
    def descriptionText: String = "Test"

    /** Force a manual to be written for each tool */
    def manualText: String = "Test"

    /** Force an example to be written for each tool */
    def exampleText: String = "Test"
  }

  object MultiTestTool2 extends MultiToolCommand {
    def subTools: Map[String, List[ToolCommand[_]]] =
      Map("mode" -> List(TestTool))

    override def extendedUsage = true

    /** Force description to be written for each tool. */
    def descriptionText: String = extendedDescriptionText

    /** Force a manual to be written for each tool */
    def manualText: String = extendedManualText

    /** Force an example to be written for each tool */
    def exampleText: String = extendedExampleText
  }

  @Test
  def testNonExtended(): Unit = {
    val usageLines = MultiTestTool1.usageText.split("\n")
    usageLines.contains("Usage for MultiTestTool1:") shouldBe true
    usageLines.contains("### Usage for mode - TestTool:") shouldBe false
    MultiTestTool1.descriptionText shouldBe "Test"
    MultiTestTool1.manualText shouldBe "Test"
    MultiTestTool1.exampleText shouldBe "Test"
  }

  @Test
  def testExtended(): Unit = {
    val usageLines = MultiTestTool2.usageText.split("\n")
    usageLines.contains("Usage for MultiTestTool2:") shouldBe true
    usageLines.contains("### Usage for mode - TestTool:") shouldBe true
    MultiTestTool2.descriptionText should not be "Test"
    MultiTestTool2.manualText should not be "Test"
    MultiTestTool2.exampleText should not be "Test"
  }

  @Test
  def testExecute(): Unit = {
    TestTool.count shouldBe 0
    MultiTestTool1.main(Array("TestTool"))
    TestTool.count shouldBe 1
    MultiTestTool1.main(Array("TestTool"))
    TestTool.count shouldBe 2
  }

  @Test
  def testWrongTool(): Unit = {
    intercept[IllegalArgumentException] {
      MultiTestTool1.main(Array("Not_exist"))
    }.getMessage shouldBe "Tool 'Not_exist' not found"
  }

}
