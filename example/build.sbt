
ThisBuild / name := "example"
ThisBuild / scalaVersion := "3.4.0"

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin, LiveReloadJSPlugin, CopyFullJSPlugin)
	.enablePlugins()
	.settings(
		name := "example",
		scalaJSUseMainModuleInitializer := true,
		livereloadPublic := Some(baseDirectory.value / "public"),
		copyFullTarget := baseDirectory.value / "public" / "assets" / "js" / "main.js"
	)



