
ThisBuild / name := "appjs"
ThisBuild / scalaVersion := "3.2.1"

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin)
	.enablePlugins(LiveReloadPlugin)
	.settings(
		name := "appjs",
		scalaJSUseMainModuleInitializer := true
	)



