name := "plsql-lint-server"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.tumblr" %% "colossus" % "0.8.3"

packageOptions += Package.ManifestAttributes("Class-Path" -> "lib/*")

enablePlugins(JavaAppPackaging)

//// tasks for moving files
//lazy val createDist = taskKey[Unit]("Creates the deployment zip file")
//
//createDist := {
//  import sys.process._
//  import java.io.File
//  // remove any existing dist
//  "rm plsql-lint-server.zip".!
//  // create a tmp directory
//  "mkdir -p target/tmp/lib".!
//  // copy files
//  "cp target/scala-2.11/plsql-lint-server_2.11-1.0.jar target/tmp/plsql-lint-server-1.0.jar".!
//  "cp -r lib/ target/tmp/".!
//  // zip files
//  Process(Seq("zip", "-r", "-9", "../../plsql-lint-server.zip", "."), new File(new File("").getAbsoluteFile, "target/tmp")).!
//  // remove tmp folder
//  "rm -r target/tmp".!
//}