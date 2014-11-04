name := """glicko2-scala"""

version := "1.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  jdbc,
  anorm,
  cache,
  ws
)