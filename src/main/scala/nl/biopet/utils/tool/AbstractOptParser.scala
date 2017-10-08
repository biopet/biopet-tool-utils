package nl.biopet.utils.tool

import nl.biopet.utils.Logging
import nl.biopet.utils

/**
  * Abstract opt parser to add default args to each biopet tool
  */
abstract class AbstractOptParser[T](cmdName: String)
    extends scopt.OptionParser[T](cmdName) {
  head("General Biopet options")
  opt[String]('l', "log_level") foreach { x =>
    x.toLowerCase match {
      case "debug" => Logging.logger.setLevel(org.apache.log4j.Level.DEBUG)
      case "info" => Logging.logger.setLevel(org.apache.log4j.Level.INFO)
      case "warn" => Logging.logger.setLevel(org.apache.log4j.Level.WARN)
      case "error" => Logging.logger.setLevel(org.apache.log4j.Level.ERROR)
      case _ =>
    }
  } text "Level of log information printed. Possible levels: 'debug', 'info', 'warn', 'error'" validate {
    case "debug" | "info" | "warn" | "error" => success
    case _ => failure("Log level must be <debug/info/warn/error>")
  }
  opt[Unit]('h', "help") foreach { _ =>
    System.err.println(this.usage)
    sys.exit(0)
  } text "Print usage"
  opt[Unit]('v', "version") foreach { _ =>
    System.err.println("Version: " + utils.Version)
    sys.exit(0)
  } text "Print version"
  head(s"\n\nOptions for $cmdName\n")
}
