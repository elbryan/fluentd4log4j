import Dependencies._

ThisBuild / scalaVersion     := "2.11.12"
ThisBuild / version          := "1.0"
ThisBuild / organization     := "io.github.elbryan"
ThisBuild / organizationName := "elbryan"

lazy val root = (project in file("."))
  .settings(
    name := "fluentd4log4j",
    libraryDependencies ++= Seq(scalaTest % Test,
      "org.fluentd" % "fluent-logger" % "0.3.3",
    "log4j" % "log4j" % "1.2.17")
  )

// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

 ThisBuild / description := "Fluentd appender compatible with log4j"
 ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
 ThisBuild / homepage    := Some(url("https://github.com/elbryan/fluentd4log4j"))
 ThisBuild / scmInfo := Some(
   ScmInfo(
     url("https://github.com/elbryan/fluentd4log4j"),
     "scm:git@github.com/elbryan/fluentd4log4j.git"
   )
 )
 ThisBuild / developers := List(
   Developer(
     id    = "io.github.elbryan",
     name  = "Fabiano Francesconi",
     email = "fabiano.francesconi 'at' gmail 'dot' com",
     url   = url("http://github.com/elbryan")
   )
 )
 ThisBuild / pomIncludeRepository := { _ => false }
 ThisBuild / publishTo := {
   val nexus = "https://oss.sonatype.org/"
   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
 }
 ThisBuild / publishMavenStyle := true
