
ThisBuild / name := "appjs"
ThisBuild / scalaVersion := "3.3.0"

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin, LiveReloadJSPlugin)
	.enablePlugins()
	.settings(
		name := "appjs",
		scalaJSUseMainModuleInitializer := true,
		//copyTo := Some(baseDirectory.value / "public" / "assets" / "js"),
		dist := Some(baseDirectory.value / "public")
	)



