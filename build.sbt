
lazy val scala212 = "2.12.18"
lazy val scala213 = "2.13.11"
lazy val scala3 = "3.3.0"
lazy val scalaVersions = List(scala212, scala213, scala3)

ThisBuild / crossScalaVersions := scalaVersions
ThisBuild / scalaVersion     := scala212
ThisBuild / version          := "0.2.3"
ThisBuild / organization     := "br.com.mobilemind"
ThisBuild / organizationName := "livereload"

githubOwner := "mobilemindtec"
githubRepository := "m2"
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")

lazy val root = (project in file("."))
  //.enablePlugins(SbtPlugin)
  .settings(
    name := "livereload",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      //"org.scalatest" %% "scalatest" % "3.2.16" % Test,
      "com.lihaoyi" %% "cask" % "0.9.1",
      "com.lihaoyi" %% "upickle" % "3.1.0",
      //"com.typesafe.play" %% "play-json" % "2.9.3"
    ),
  )
