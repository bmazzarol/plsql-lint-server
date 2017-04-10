name := "plsql-lint-server"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.tumblr" %% "colossus" % "0.8.3"

packageOptions += Package.ManifestAttributes("Class-Path" -> "lib/*")

enablePlugins(JavaAppPackaging)