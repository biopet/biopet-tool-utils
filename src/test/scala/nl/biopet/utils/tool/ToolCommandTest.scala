package nl.biopet.utils.tool

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

class ToolCommandTest extends TestNGSuite with Matchers {

  case class TestArgs(num: Int = 1)

  object TestTool extends ToolCommand[TestArgs] {

    var count = 0

    def main(args: Array[String]): Unit = {
      val cmdArgs = this.cmdArgs(args)

      count = cmdArgs.num
    }

    /** This is the parser object that will be tested. */
    def argsParser: AbstractOptParser[TestArgs] = new AbstractOptParser[TestArgs]("test") {
      opt[Int]('n', "num") action { (a,b) => b.copy(num = a)}
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
}
