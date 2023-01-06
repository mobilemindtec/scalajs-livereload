
lazy val scala212 = "2.12.16"
lazy val scala321 = "3.2.1"
lazy val supportedScalaVersions = List(scala212, scala321)

ThisBuild / scalaVersion     := scala212
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "br.com.mobilemind"
ThisBuild / organizationName := "livereload"


lazy val root = (project in file("."))
  //.enablePlugins(SbtPlugin)
  .settings(
    name := "livereload",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.14" % Test,
      "com.lihaoyi" %% "cask" % "0.8.3",
      "com.typesafe.play" %% "play-json" % "2.9.3"
    )
  )
