assemblySettings

name := "analyzer"

version := "1.0"

scalaVersion := "2.11.0"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.5.0.201409260305-r",
  "org.gitective" % "gitective-core" % "0.9.9",
  "io.spray" %%  "spray-json" % "1.2.6",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "mysql" % "mysql-connector-java" % "5.1.30",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)
