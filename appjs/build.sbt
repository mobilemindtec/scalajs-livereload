
lazy val scala212 = "2.12.16"
lazy val scala321 = "3.2.1"
lazy val supportedScalaVersions = List(scala212, scala321)

ThisBuild / name := "appjs"
ThisBuild / scalaVersion := scala321

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin)
	.enablePlugins(LiveReloadPlugin)
	.settings(
		name := "appjs",
		libraryDependencies ++= Seq(
			"com.raquo" %%% "laminar" % "0.13.1"
		),
		scalaJSUseMainModuleInitializer := true
	)



