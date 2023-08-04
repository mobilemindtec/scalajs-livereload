addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.2")

lazy val liveReloadPlugin = RootProject(file("../.."))

lazy val root = project
	.in(file("."))
	.dependsOn(liveReloadPlugin)
