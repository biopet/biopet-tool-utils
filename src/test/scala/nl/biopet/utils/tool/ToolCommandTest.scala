package nl.biopet.utils.tool

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

class ToolCommandTest extends TestNGSuite with Matchers {

  object TestTool extends ToolCommand {

    case class Args()

    def parser[Args]: AbstractOptParser[Args] = new AbstractOptParser[Args]("test") {}

    def main(args: Array[String]): Unit = {
      count += 1
    }
    var count = 0
  }

  @Test
  def test(): Unit = {
    TestTool.count shouldBe 0
    TestTool.main(Array("test"))
    TestTool.toolName shouldBe "TestTool"
    TestTool.count shouldBe 1
  }
}
