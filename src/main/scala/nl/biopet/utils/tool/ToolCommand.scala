/*
 * Copyright (c) 2014 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.utils.tool

import java.io.{File, PrintWriter}

import nl.biopet.utils.{Logging, Documentation}
import nl.biopet.utils.io._

import scala.collection.mutable.ListBuffer

/**
  * Trait for biopet tools, sets some default args, adds documentation generator
  */
trait ToolCommand[Args] extends Logging {

  /** This will return the name of the tool */
  def toolName: String = this.getClass.getSimpleName.stripSuffix("$")

  /** This is the tool name to be used in URL. Can be overwritten. */
  def urlToolName: String = toolName.toLowerCase()

  /** This is the main entry point of the tool */
  def main(args: Array[String])

  //ARGPARSER DEFINITION

  /** This is the parser object that will be tested. */
  def argsParser: AbstractOptParser[Args]

  /** Returns an empty/default args case class */
  def emptyArgs: Args

  /** Converts args to a Args case class */
  def cmdArrayToArgs(args: Array[String]): Args = {
    argsParser
      .parse(args, emptyArgs)
      .getOrElse(throw new IllegalArgumentException)
  }

  // DOCUMENTATION TEXT

  /** Force description to be written for each tool. */
  def descriptionText: String

  /** Force a manual to be written for each tool */
  def manualText: String

  /** Force an example to be written for each tool */
  def exampleText: String

  /** Universal text for pointing to the documentation.*/
  def documentationText: String =
    s"For documentation and manuals visit our [github.io page](https://biopet.github.io/$urlToolName)."

  /** Universal contact text */
  def contactText: String =
    s"""
       |<p>
       |  <!-- Obscure e-mail address for spammers -->
       |For any question related to $toolName, please use the
       |<a href='https://github.com/biopet/$urlToolName/issues'>github issue tracker</a>
       |or contact
       | <a href='http://sasc.lumc.nl/'>the SASC team</a> directly at: <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;&#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;'>
       |&#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;</a>.
       |</p>
       |
     """.stripMargin

  /** Universal text referring to BIOPET. */
  def aboutText: String =
    s"""
      |$toolName is part of BIOPET tool suite that is developed at LUMC by [the SASC team](http://sasc.lumc.nl/).
      |Each tool in the BIOPET tool suite is meant to offer a standalone function that can be used to perform a
      |dedicate data analysis task or added as part of [BIOPET pipelines](http://biopet-docs.readthedocs.io/en/latest/).
      |
      |All tools in the BIOPET tool suite are [Free/Libre](https://www.gnu.org/philosophy/free-sw.html) and
      |[Open Source](https://opensource.org/osd) Software.
    """.stripMargin

  /** Universal contributing text */
  def contributingText: String =
    s"""The source code of $toolName can be found [here](https://github.com/biopet/$urlToolName).
       |We welcome any contributions. Bug reports, feature requests and feedback can be submitted at our
       |[issue tracker](https://github.com/biopet/$urlToolName/issues).
       |
       |$toolName is build using sbt. Before submitting a pull request, make sure all tests can be passed by
       |running `sbt test` from the project's root. We recommend using an IDE to work on $toolName. We have had
       |good results with [this IDE](https://www.jetbrains.com/idea/).
       |""".stripMargin

  /** Universal installation text */
  def installationText: String =
    s"""
       |$toolName requires Java 8 to be installed on your device. Download Java 8
       |[here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
       |or install via your distribution's package manager.
       |
       |Download the latest version of $toolName [here](https://github.com/biopet/$urlToolName/releases/).
       |To generate the usage run:
       |
       |    java -jar <${toolName}_jar> --help
       |
    """.stripMargin

  /** Which chapters should be in the README */
  def readmeContents: List[(String, String)] = {
    List(
      (s"# $toolName", descriptionText),
      ("# Documentation", documentationText),
      ("# About", aboutText),
      ("# Contact", contactText)
    )
  }

  /** Which chapters should be on the tool documentation's main page */
  def mainPageContents: List[(String, String)] = {
    List(
      (s"# Description", descriptionText),
      ("# Installation", installationText),
      ("# Manual", manualText),
      ("## Example", exampleText),
      ("## Usage", usageText),
      ("# About", aboutText),
      ("# Contributing", contributingText),
      ("# Contact", contactText)
    )
  }

  // DOCUMENTATION METHODS

  def validateArgs(args: String*): Unit = {
    cmdArrayToArgs(args.toArray)
  }

  /** Convert and tests args */
  def example(args: String*): String = {
    validateArgs(args: _*)

    exampleToMarkdown(spark = false, args: _*)
  }

  /** Convert and *not* tests args */
  def unsafeExample(args: String*): String = {
    exampleToMarkdown(spark = false, args: _*)
  }

  /** Convert and tests args */
  def sparkExample(args: String*): String = {
    validateArgs(args: _*)

    exampleToMarkdown(spark = true, args: _*)
  }

  /** Convert and *not* tests args */
  def sparkUnsafeExample(args: String*): String = {
    exampleToMarkdown(spark = true, args: _*)
  }

  /** Common function to convert to string */
  private[tool] def exampleToMarkdown(spark: Boolean, args: String*): String = {
    val argumentsList = args.mkString(" ").split(" ")
    val example = new StringBuffer()
    if (spark)
      example.append(
        s"\n\n    spark-submit <spark arguments> <${toolName}_jar>")
    else example.append(s"\n\n    java -jar <${toolName}_jar>")
    for (argument <- argumentsList) {
      if (argument.startsWith("-")) {
        example.append(" \\\n    " + argument)
      } else {
        example.append(" " + argument)
      }
    }
    example.append("\n\n")
    example.toString
  }

  protected def usageHeaders: List[String] =
    List("Option", "Required", "Can occur multiple times", "Description")

  protected def usageRows(
      parser: scopt.OptionParser[Args]): List[List[String]] = {
    val body = new ListBuffer[List[String]]

    for (option <- parser.optionsForRender) {
      // Do not show usage if option is hidden.
      if (!option.isHidden) {
        val shortOpt: String =
          if (option.shortOpt.isDefined) "-" + option.shortOpt.get else ""
        val optSeperator: String =
          if (option.shortOpt.isDefined) ", " else ""
        val name: String = option.fullName + optSeperator + shortOpt
        val description: String = option.desc

        val required: String = option.getMinOccurs match {
          case 1      => "yes"
          case 0      => "no"
          case number => s"yes ($number required)"
        }

        val occurances: String = option.getMaxOccurs match {
          case 1            => "no"
          case Int.MaxValue => s"yes (unlimited)"
          case number       => s"yes ($number times)"
        }
        val tableRow: List[String] =
          List(name, required, occurances, description)
        body.append(tableRow)
      }
    }
    body.toList
  }

  def usageHtmlTable: String =
    Documentation.htmlTable(usageHeaders, usageRows(argsParser))

  /** Creates a html formatted usage string */
  def usageText: String = {

    // Generate usage table by defining body and headers.
    s"Usage for $toolName:\n\n$usageHtmlTable"
  }

  /**
    * Generates the README
    * @param outputFile The readme file
    */
  def generateReadme(outputFile: File): Unit = {
    Documentation.contentsToMarkdown(readmeContents, outputFile)
  }

  /**
    * Outputs markdown documentation for LAIKA processing.
    * @param outputDirectory outputs the Markdown documentation in this directory
    */
  def generateDocumentation(outputDirectory: File,
                            version: String,
                            release: Boolean = false): Unit = {

    val versionDirectory =
      if (release) new File(outputDirectory, version)
      else new File(outputDirectory, "develop")
    versionDirectory.mkdirs()

    val cssDirectory = new File(versionDirectory, "css")
    Documentation.contentsToMarkdown(mainPageContents,
                                     new File(versionDirectory, "index.md"))
    resourceToFile("/nl/biopet/utils/tool/default.template.html",
                   new File(versionDirectory, "default.template.html"))
    resourceToFile("/nl/biopet/utils/tool/docs.css",
                   new File(cssDirectory, "docs.css"))

    val configFile = new PrintWriter(
      new File(versionDirectory, "/directory.conf"))
    val navigationOrder = List(
      "index.md"
    ).mkString("\n")

    val config: String = {
      s"""title = "$toolName"
         |urlToolName = "$urlToolName"
         |
          """.stripMargin + "\nnavigationOrder = [\n" +
        navigationOrder + "\n]"
    }
    configFile.write(config)
    configFile.close()

    if (release) {
      Documentation.htmlRedirector(
        outputFile = new File(outputDirectory, "index.html"),
        link = s"./$version/index.html",
        title = s"$toolName Documentation",
        redirectText = s"Click here to go to $toolName documentation."
      )
    }
  }

}
