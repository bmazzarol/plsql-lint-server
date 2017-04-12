name := "plsql-lint-server"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"

libraryDependencies ++= Seq(
  "com.tumblr" %% "colossus" % "0.8.3",
  "org.spire-math" %% "jawn-ast" % "0.10.4")

packageOptions += Package.ManifestAttributes("Class-Path" -> "lib/*")

enablePlugins(JavaAppPackaging)