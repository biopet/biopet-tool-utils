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

import nl.biopet.utils.Logging

/**
  * Trait for biopet tools, sets some default args
  */
trait ToolCommand[Args] extends Logging with ToolDocumentation {

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

  def usageText: String = {
    // Prepend code block with four spaces in concordance with Markdown specification.
    val usage = new StringBuffer()
    val usageLines: Array[String] = argsParser.usage.split("\n")
    usageLines.foreach(line => usage.append("    " + line + System.lineSeparator()))
    usage.toString
  }
}
