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

import nl.biopet.utils.{IoUtils, Logging, Documentation}

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
       |  <a href='http://sasc.lumc.nl/'>the SASC team</a> directly at: <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;
       | &#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;'>
       |  &#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;</a>.
       |</p>
       |
     """.stripMargin

  /** Universal text referring to BIOPET. */
  def aboutText: String =
    s"""
      |$toolName is part of BIOPET tool suite that is developed at LUMC by [the SASC team](http://sasc.lumc.nl/).
      |Each tool in the BIOPET tool suite is meant to offer a standalone function that can be used to perform a
      |dedicate data analysis task or added as part of [BIOPET pipelines](http://biopet-docs.readthedocs.io/en/latest/).
    """.stripMargin

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
       |    java -jar $toolName --version.jar --help
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
      ("# Contact", contactText)
    )
  }

  // DOCUMENTATION METHODS

  /** Creates a html formatted usage string */
  def usageText: String = {

    // Generate usage table by defining body and headers.
    val headers: List[String] =
      List("Option", "Required", "Can occur multiple times", "Description")

    def body: List[List[String]] = {
      val body = new ListBuffer[List[String]]

      for (option <- argsParser.optionsForRender) {
        // Do not show usage if option is hidden.
        if (!option.isHidden) {
          val shortOpt: String =
            if (option.shortOpt.isDefined) "-" + option.shortOpt.get else ""
          val optSeperator: String =
            if (option.shortOpt.isDefined) ", " else ""
          val name: String = option.fullName + optSeperator + shortOpt
          val description: String = option.desc

          val required: String = option.getMinOccurs match {
            case 1 => "yes"
            case 0 => "no"
            case number => s"yes ($number required)"
          }

          val occurances: String = option.getMaxOccurs match {
            case 1 => "no"
            case Int.MaxValue => s"yes (unlimited)"
            case number => s"yes ($number times)"
          }
          val tableRow: List[String] =
            List(name, required, occurances, description)
          body.append(tableRow)
        }
      }
      body.toList
    }
    s"Usage for $toolName:\n" + Documentation.htmlTable(headers, body)
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
                            redirect: Boolean = false): Unit = {

    val versionDirectory = new File(outputDirectory, version)
    versionDirectory.mkdirs()

    val cssDirectory = new File(versionDirectory, "css")
    Documentation.contentsToMarkdown(mainPageContents,
                                     new File(versionDirectory, "index.md"))
    IoUtils.resourceToFile("/nl/biopet/utils/tool/default.template.html",
                           new File(versionDirectory, "default.template.html"))
    IoUtils.resourceToFile("/nl/biopet/utils/tool/docs.css",
                           new File(cssDirectory, "docs.css"))

    val configFile = new PrintWriter(
      new File(versionDirectory, "/directory.conf"))
    val navigationOrder = List(
      "index.md"
    ).mkString("\n")

    val config: String = {
      s"""title = "$toolName"
         |
          """.stripMargin + "\nnavigationOrder = [\n" +
        navigationOrder + "\n]"
    }
    configFile.write(config)
    configFile.close()

    if (redirect) {
      Documentation.htmlRedirector(
        outputFile = new File(outputDirectory, "index.html"),
        link = s"./$version/index.html",
        title = s"${toolName} Documentation",
        redirectText = s"Click here to go to ${toolName} documentation."
      )
    }
  }

}
