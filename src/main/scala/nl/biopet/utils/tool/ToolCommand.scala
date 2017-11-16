/**
  * Biopet is built on top of GATK Queue for building bioinformatic
  * pipelines. It is mainly intended to support LUMC SHARK cluster which is running
  * SGE. But other types of HPC that are supported by GATK Queue (such as PBS)
  * should also be able to execute Biopet tools and pipelines.
  *
  * Copyright 2014 Sequencing Analysis Support Core - Leiden University Medical Center
  *
  * Contact us at: sasc@lumc.nl
  *
  * A dual licensing mode is applied. The source code within this project is freely available for non-commercial use under an AGPL
  * license; For commercial users or users who do not want to follow the AGPL
  * license, please contact us to obtain a separate license.
  */
package nl.biopet.utils.tool

import java.io.{File, PrintWriter}

import nl.biopet.utils.Logging

import scala.io.Source

/**
  * Trait for biopet tools, sets some default args
  */
trait ToolCommand[Args] extends Logging {

  /** This will return the name of the tool */
  def toolName: String = this.getClass.getSimpleName.stripSuffix("$")

  /** This is the tool name to be used in URL. Can be overwritten. */
  def urlToolName: String = toolName.toLowerCase()

  /** This is the main entry point of the tool */
  def main(args: Array[String])

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

  /** Creates a usage and prepends code block with four spaces in concordance with Markdown specification. */
  def usageText: String = {

    // Generate usage
    val usage = new StringBuffer()
    usage.append(s"options for ${toolName}:\n\n")
    for (option <- argsParser.optionsForRender) {
      val shortOpt: String = option.shortOpt.getOrElse("")
      val optSeperator: String = if (option.shortOpt != None) ", " else ""
      val name: String = option.fullName + optSeperator + shortOpt
      val description: String = option.desc
      val optionUsage: String =  f"$name%-35s" + description + "\n"
      usage.append(optionUsage)
    }

    // Format the usage for markdown.
    val markdownFormattedUsage = new StringBuffer()
    val usageLines: Array[String] = usage.toString.split("\n")
    usageLines.foreach(line => markdownFormattedUsage.append("    " + line + "\n"))
    markdownFormattedUsage.toString
  }

  /** Force description to be written for each tool. */
  def descriptionText: String

  /** Force a manual to be written for each tool */
  def manualText: String

  /** Force an example to be written for each tool */
  def exampleText: String

  /** Universal text for pointing to the documentation.*/
  // TODO: Change link.
  def documentationText: String =
    s"For documentation and manuals visit our [github.io page](https://biopet.github.io/${urlToolName})."

  /** Universal contact text */
  def contactText: String =
    s"""
       |<p>
       |  <!-- Obscure e-mail address for spammers -->
       |For any question related to this tool, please use the
       |<a href='https://github.com/biopet/${urlToolName}/issues'>github issue tracker</a>
       |or contact
       |  <a href='http://sasc.lumc.nl/'>the SASC team</a> directly at: <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;
       | &#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;'>
       |  &#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;</a>.
       |</p>
       |
     """.stripMargin

  /** Universal text referring to BIOPET. */
  def aboutText: String =
    """
      |This tool is part of BIOPET tool suite that is developed at LUMC by [the SASC team](http://sasc.lumc.nl/).
      |Each tool in the BIOPET tool suite is meant to offer a standalone function that can be used to perform a
      |dedicate data analysis task or added as part of [BIOPET pipelines](http://biopet-docs.readthedocs.io/en/latest/).
    """.stripMargin

  /** Universal installation text */
  def installationText: String =
    s"""
       |This tool requires Java 8 to be installed on your device. Download Java 8
       |[here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
       |or install via your distribution's package manager.
       |
       |Download the latest version of ${toolName} [here](https://github.com/biopet/${urlToolName}/releases/).
       |To generate the usage run:
       |
       |    java -jar ${toolName}-version.jar --help
       |
    """.stripMargin

  /** Which chapters should be in the README */
  def readmeContents: List[(String, String)] = {
    List(
      (s"# ${toolName}", descriptionText),
      ("# Documentation", documentationText),
      ("# About", aboutText),
      ("# Contact", contactText)
    )
  }

  /**
    * Generates a Markdown file from a list of chapters (heading, content) tuples.
    * @param contents A list of (string, string) tuples, where the first string is the title and the other the content.
    * @param outputFile The output filename to which the markdown file is written.
    */
  def contentsToMarkdown(
      contents: List[(String, String)],
      outputFile: File
  ): Unit = {
    outputFile.getParentFile.mkdirs()
    val fileWriter = new PrintWriter(outputFile)
    for ((head, content) <- contents) {
      fileWriter.println(head)
      fileWriter.println()
      fileWriter.println(content)
      fileWriter.println()
    }
    fileWriter.close()
  }

  /**
    * Converts a resource to a file
    * @param resource Which resource
    * @param outputFile Filename for the output file
    */
  def resourceToFile(resource: String, outputFile: File): Unit = {
    outputFile.getParentFile.mkdirs()
    val printWriter = new PrintWriter(outputFile)
    val source = getClass.getResourceAsStream(resource)
    val lines: Iterator[String] = Source.fromInputStream(source).getLines
    lines.foreach(line => printWriter.println(line))
    printWriter.close()
  }

  /**
    * Generates the README
    * @param outputFile The readme file
    */
  def generateReadme(outputFile: File): Unit = {
    contentsToMarkdown(readmeContents, outputFile)
  }

  /**
    * Outputs markdown documentation for LAIKA processing.
    * @param docsDir outputs the Markdown documentation in this directory
    */
  def generateDocumentation(docsDir: String): Unit = {
    val outputDirectory = new File(docsDir)
    outputDirectory.mkdirs()
    val mainPageContents: List[(String, String)] = {
      List(
        (s"# ${toolName}", descriptionText),
        ("# Installation", installationText),
        ("# Manual", manualText),
        ("## Example", exampleText),
        ("## Usage", usageText),
        ("# About", aboutText),
        ("# Contact", contactText)
      )
    }

    contentsToMarkdown(mainPageContents, new File(docsDir + "/index.md"))
    resourceToFile("/default.template.html",
                   new File(docsDir + "default.template.html"))
    resourceToFile("/bootstrap.css", new File(docsDir + "/css/bootstrap.css"))
    resourceToFile("/docs.css", new File(docsDir + "/css/docs.css"))

    val configFile = new PrintWriter(new File(docsDir + "directory.conf"))
    val navigationOrder = List(
      "index.md"
    ).mkString("\n")

    val config: String = {
      s"""title = "${toolName}"
         |
          """.stripMargin + System.lineSeparator() +
        "navigationOrder = [" + System.lineSeparator() +
        navigationOrder + System.lineSeparator() + "]"
    }
    configFile.write(config)
    configFile.close()
  }

}
