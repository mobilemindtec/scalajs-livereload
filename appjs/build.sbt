
ThisBuild / name := "appjs"
ThisBuild / scalaVersion := "3.2.1"

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin, LiveReloadJSPlugin)
	.enablePlugins()
	.settings(
		name := "appjs",
		scalaJSUseMainModuleInitializer := true,
		copyTarget := Some(baseDirectory.value / "target")
	)



