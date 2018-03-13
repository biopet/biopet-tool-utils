package nl.biopet.utils.tool.multi

import nl.biopet.utils.tool.ToolCommand

trait MultiToolCommand extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    args.headOption match {
      case Some(name) =>
        singleTool(name).main(args.tail)
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
}
