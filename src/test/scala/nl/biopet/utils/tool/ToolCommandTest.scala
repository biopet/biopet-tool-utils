package nl.biopet.utils.tool

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

class ToolCommandTest extends TestNGSuite with Matchers {
  @Test
  def test(): Unit = {
    var a: Array[String] = Array()
    val tool = new ToolCommand {
      def main(args: Array[String]): Unit = {
        a = args
      }
    }
    tool.main(Array("test"))
    a shouldBe Array("test")
  }
}
