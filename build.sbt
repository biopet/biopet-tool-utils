organization := "com.github.biopet"
organizationName := "Biopet"
name := "tool-utils"

biopetUrlName := "tool-utils"

startYear := Some(2014)

biopetIsTool := false

developers += Developer(id = "ffinfo",
                        name = "Peter van 't Hof",
                        email = "pjrvanthof@gmail.com",
                        url = url("https://github.com/ffinfo"))

crossScalaVersions := Seq("2.11.12", "2.12.5")

scalaVersion := "2.11.12"

libraryDependencies += "com.github.biopet" %% "common-utils" % "0.4-SNAPSHOT" changing ()
libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"

libraryDependencies += "com.github.biopet" %% "test-utils" % "0.3" % Test
