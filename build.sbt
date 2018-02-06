organization := "com.github.biopet"
name := "ToolUtils"

biopetUrlName := "tool-utils"

biopetIsTool := false

developers += Developer(id="ffinfo", name="Peter van 't Hof", email="pjrvanthof@gmail.com", url=url("https://github.com/ffinfo"))

scalaVersion := "2.11.11"

libraryDependencies += "com.github.biopet" %% "common-utils" % "0.3-SNAPSHOT" changing()
libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"

libraryDependencies += "com.github.biopet" %% "test-utils" % "0.2" % Test
