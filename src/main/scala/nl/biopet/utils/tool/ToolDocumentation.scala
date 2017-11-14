package nl.biopet.utils.tool

trait ToolDocumentation {
  /** This will return the name of the tool */
  def toolName: String = this.getClass.getSimpleName.stripSuffix("$")

  // Force description to be written for each tool.
  def descriptionText: String

  // Force a manual to be written for each tool
  def manualText: String

  // Force an example to be written for each tool
  def exampleText: String

  // Universal text for pointing to the documentation.
  // TODO: Change link.
  def documentationText: String = s"For documentation and manuals visit our [github.io page](https://biopet.github.io/${toolName}"

  // Universal contact text
  def contactText: String =
    s"""
       |<p>
       |  <!-- Obscure e-mail address for spammers -->
       |For any question related to this tool, please use the
       |[github issue tracker](https://github.com/biopet/${toolName}/issues)
       |or contact
       |  <a href='http://sasc.lumc.nl/'>the SASC team</a> directly at: <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;
       | &#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;'>
       |  &#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;</a>.
       |</p>
       |
     """.stripMargin

  // Universal text referring to BIOPET.
  def aboutText: String =
    """
      |This tool is part of BIOPET tool suite that is developed at LUMC by [the SASC team](http://sasc.lumc.nl/).
      |Each tool in the BIOPET tool suite is meant to offer a standalone function that can be used to perform a
      |dedicate data analysis task or added as part of [BIOPET pipelines](http://biopet-docs.readthedocs.io/en/latest/).
    """.stripMargin

  // Universal installation text
  def installationText: String =
    s"""
      |This tool requires Java 8 to be installed on your device. Download Java 8
      |[here](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
      |or install via your distribution's package manager.
      |
      |Download the latest version of ${toolName} [here](https://github.com/biopet/${toolName}/releases/).
      |To generate the usage run:
      |```bash
      |java -jar ${toolName}-version.jar --help
      |```
    """.stripMargin

  def readmeContents: List[(String,String)] = {
    List(
      (s"$toolName",descriptionText),
      ("Documentation", documentationText),
      ("About", aboutText),
      ("Contact", contactText)

    )
  }

  def contentsToMarkdown(contents: List[(String, String)]
                   ): Unit

  def generateReadme: Unit
  def generateDocumentation: Unit
}
