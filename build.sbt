name := "dynamite"
organization := "org.dynamite"
version := "0.0.1"
isSnapshot := true

scalaVersion := "2.11.8"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.4"
libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
libraryDependencies += "com.github.louis-forite" %% "dynamo-ast" % "0.1"
libraryDependencies += "org.specs2" %% "specs2-core" % "3.8.4" % Test
libraryDependencies += "org.specs2" %% "specs2-scalacheck" % "3.8.4" % Test
libraryDependencies += "com.github.tomakehurst" % "wiremock" % "2.1.11" % Test

val circeVersion = "0.7.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-feature",
  "-deprecation:false",
  "-Xlint",
  "-Xcheckinit",
  "-Ywarn-unused-import",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Yno-adapted-args",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding", "UTF-8")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"
