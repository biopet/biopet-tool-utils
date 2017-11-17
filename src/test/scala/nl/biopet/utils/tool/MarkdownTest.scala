package nl.biopet.utils.tool

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class MarkdownTest extends BiopetTest {
  @Test
  def testTableMethod(): Unit = {
    the [java.lang.IllegalArgumentException] thrownBy {
      Markdown.htmlTable(
        List("Column1", "Column2"),
        List(
          List("1","2"),
          List("a","b","c")
        )
      ) } should have message "requirement failed: Number of items in each row should be equal number of items in header."

  }
}
