
ThisBuild / name := "example"
ThisBuild / scalaVersion := "3.3.0"

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin, LiveReloadJSPlugin)
	.enablePlugins()
	.settings(
		name := "example",
		scalaJSUseMainModuleInitializer := true,
		livereloadPublic := Some(baseDirectory.value / "public")
	)



