name := "analyzer"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.3.2.201404171909-r",
  "org.gitective" % "gitective-core" % "0.9.9",
  "org.json4s" %% "json4s-native" % "3.2.9",
  "org.json4s" %% "json4s-ext" % "3.2.9",
  "com.typesafe.slick" % "slick_2.11.0-RC4" % "2.1.0-M1", // TODO: use scala version 2.11.0 when available
  "mysql" % "mysql-connector-java" % "5.1.30",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)
