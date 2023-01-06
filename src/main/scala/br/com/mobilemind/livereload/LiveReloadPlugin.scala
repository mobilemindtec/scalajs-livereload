package br.com.mobilemind.livereload

import sbt.Keys._
import sbt._

object LiveReloadPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    val livereload = taskKey[Unit]("starts live reload")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    livereload := {
      val s = streams.value
      Server.start(new PluginLogger(s.log))
    },
    Compile / compile := {
      val c = (Compile / compile).value
      val s = streams.value
      Server.notify(new PluginLogger(s.log))
      c
    }
  )
}