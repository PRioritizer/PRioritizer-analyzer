name := "analyzer"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.2.201404171909-r",
  "org.gitective" % "gitective-core" % "0.9.9",
  "org.json4s" %% "json4s-native" % "3.2.9",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)
