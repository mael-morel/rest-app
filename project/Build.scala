import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName = "mongo-app"
  val appVersion = "1.0-SNAPSHOT"

  scalaVersion := "2.10.2"

  val appDependencies = Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
    "org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases"
      // settings
  )

}
