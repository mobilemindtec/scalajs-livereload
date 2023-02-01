package br.com.mobilemind.livereload

import sbt.Keys.*
import sbt.*

import java.io.File

object LiveReloadJSPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    var copyFrom = SettingKey[Option[File]]("copyFrom", "scala.js linker artifact copy target directory")
    val copyTarget = SettingKey[Option[File]]("copyTarget", "scala.js linker artifact copy target directory")
    val livereload = taskKey[Unit]("starts live reload js")
    val copyFilesJS = taskKey[Unit]("copy js files")
    val livewatch = taskKey[Unit]("copy js files")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    copyFrom := None,
    copyTarget := None,
    livereload := {
      val s = streams.value
      Server.start(new PluginLogger(s.log))
    },
    copyFilesJS := {
      val targetOutPath = (Compile / crossTarget).value
      val dist = new File(targetOutPath, s"${name.value}-fastopt")
      val copySrcPath = copyFrom.value.getOrElse(dist)

      if(copyTarget.value.isEmpty){
        println(s"[info] LiveReloadPlugin: copyTarget is not defined")
      }else {
        val target = copyTarget.value.get
        println(s"[info] LiveReloadPlugin: Start copy js watch from ${copySrcPath}, copy to ${target}")
        FileWatcher.start(copySrcPath, target)
      }
    },
    Compile / compile := {
      val c = (Compile / compile).value
      val s = streams.value
      Server.notify(new PluginLogger(s.log))
      c
    },
    (Global / onUnload) := {
      (Global / onUnload).value.compose { state =>
        print("LiveReloadPlugin: stop all")
        FileWatcher.stop()
        Server.stop()
        state
      }
    },
    livewatch := livereload.dependsOn(Def.task(copyFilesJS.value)).value
  )
}