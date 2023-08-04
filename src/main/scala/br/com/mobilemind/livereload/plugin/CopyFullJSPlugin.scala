package br.com.mobilemind.livereload.plugin

import sbt.*
import Keys.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

// this does not adjust the content of the source map at all!
object CopyFullJSPlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin
  final object autoImport {
    val copyFullTarget = SettingKey[File]("copyTarget", "scala.js linker artifact copy target directory")
    val copyFullJS     = TaskKey[Unit]("copyJS", "Copy scala.js linker artifacts to another location after linking.")
  }
  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[Task[Unit]]] = Seq(
    copyFullJS := copyJSTask.value,
    fastOptJS / copyFullJS := (copyFullJS triggeredBy (Compile / fastOptJS)).value,
    fullOptJS / copyFullJS := (copyFullJS triggeredBy (Compile / fullOptJS)).value
  )
  //define inline in autoImport via `copyJSTask := {` or separately like this
  private def copyJSTask = Def.task {
    val logger = streams.value.log
    val odir = copyFullTarget.value
    val src = (Compile / scalaJSLinkedFile).value.data
    val isJsFileName = odir.getCanonicalPath.endsWith(".js")
    val fileName = if (isJsFileName) odir.name else src.name
    val destPath = if (isJsFileName) odir.getParentFile else odir

    logger.info(s"Copying artifacts [js,map] from ${src.getParent} to [${destPath.getCanonicalPath}]")

    IO.copy(
      Seq(
        (src, destPath / fileName),
        (file(src.getCanonicalPath + ".map"), destPath / (fileName + ".map"))
      ),
      CopyOptions(
        overwrite = true,
        preserveLastModified = true,
        preserveExecutable = true
      )
    )
  }
}