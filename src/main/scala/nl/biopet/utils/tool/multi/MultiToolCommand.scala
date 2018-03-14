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

package nl.biopet.utils.tool.multi

import nl.biopet.utils.tool.ToolCommand

trait MultiToolCommand extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    val cmdArg = cmdArrayToArgs(args)
    cmdArg.toolName match {
      case Some(name) =>
        singleTool(name).main(cmdArg.toolArgs)
      case _ =>
        printToolList()
        throw new IllegalArgumentException(s"Please supply a tool name")
    }
  }

  def singleTool(name: String): ToolCommand[_] = {
    allTools.find(_.toolName.toLowerCase == name).getOrElse {
      printToolList()
      throw new IllegalArgumentException(s"Tool '$name' not found")
    }
  }

  def allTools: List[ToolCommand[_]] =
    subTools.flatMap { case (_, tools) => tools }.toList

  def printToolList(): Unit = {
    argsParser.usage.split("\n").foreach(logger.info)
    logger.info("")
    subTools.foreach { case (group, tools) => printToolList(group, tools) }
  }

  def printToolList(title: String, tools: List[ToolCommand[_]]): Unit = {
    logger.info(s"** $title **")
    tools
      .map(_.toolName)
      .grouped(6)
      .map(x => x.mkString(", "))
      .foreach(logger.info)
    logger.info("")
  }

  def subTools: Map[String, List[ToolCommand[_]]]

  private def validateArgs(args: String*): Unit = {
    args.headOption match {
      case Some(name) => singleTool(name).cmdArrayToArgs(args.tail.toArray)
      case _          =>
    }
  }

  override def sparkExample(args: String*): String = {
    validateArgs(args: _*)
    exampleToMarkdown(spark = true, args: _*)
  }

  override def example(args: String*): String = {
    validateArgs(args: _*)
    exampleToMarkdown(spark = false, args: _*)
  }

  def extendedUsage = false

  override def usageText: String = {
    if (extendedUsage) {
      super.usageText +
        subTools
          .map {
            case (group, tools) =>
              tools
                .map { tool =>
                  s"### Usage for $group - ${tool.toolName}:\n\n" +
                    tool.usageHtmlTable
                }
                .mkString("\n\n")
          }
          .mkString("\n\n")
    } else super.usageText
  }

  def extendedDescriptionText: String = {
    subTools
      .map {
        case (group, tools) =>
          tools
            .map { tool =>
              s"""
          |#### $group - ${tool.toolName}
          |${tool.descriptionText}
        """.stripMargin
            }
            .mkString("\n")
      }
      .mkString("\n")
  }

  def extendedManualText: String = {
    subTools
      .map {
        case (group, tools) =>
          tools
            .map { tool =>
              s"""
           |#### $group - ${tool.toolName}
           |${tool.manualText}
        """.stripMargin
            }
            .mkString("\n")
      }
      .mkString("\n")
  }

  def extendedExampleText: String = {
    subTools
      .map {
        case (group, tools) =>
          tools
            .map { tool =>
              s"""
           |#### $group - ${tool.toolName}
           |${tool.exampleText}
        """.stripMargin
            }
            .mkString("\n")
      }
      .mkString("\n")
  }
}
