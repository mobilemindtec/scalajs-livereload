package br.com.mobilemind.livereload.plugin

import br.com.mobilemind.livereload.core.{FileWatcher, Server, ServerConfigs, WsSession}
import sbt.*
import sbt.Keys.*

import scala.collection.JavaConverters.*
import java.io.File
import java.nio.file.{FileSystems, Files}
import scala.util.{Failure, Success}

object WatcherUtil {

  def watch(extensions: List[String], debug: Boolean, logger: CustomLogger, target: File, dest: Option[File], notify: Boolean): Unit = {
    FileWatcher.create(extensions, debug).start(target, dest) {
      (_, _) => if(notify) Server.notify(logger)
    } match {
      case Failure(ex) => logger.error(ex.getMessage)
      case _ => logger.info(s"watch `${target.getAbsolutePath}` successful started, copy to: ${dest.map(_.getAbsolutePath).getOrElse("no copy")}")
    }
  }

  def getAllDirs(f: File): List[File] = {
    val dir = FileSystems.getDefault.getPath(f.getAbsolutePath)
    val dirs = Files
      .list(dir)
      .toList
      .asScala
      .map(_.toFile)
      .filter(_.isDirectory)
      .toList
    dirs ::: dirs.flatMap(x => getAllDirs(x))
  }
}

object LiveReloadJSPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    var watchTarget = SettingKey[Option[File]]("watchTarget", "scala.js linker artifact copy target directory")
    val copyTo = SettingKey[Option[File]]("copyTo", "scala.js linker artifact copy target directory")
    val dist = SettingKey[Option[File]]("dist", "static dir to serve")
    val distJsFolder = SettingKey[Option[String]]("distJsFolder", "static dir to serve")
    val watchDist = SettingKey[Option[Boolean]]("watchDist", "static dir to serve")
    val debug = SettingKey[Option[Boolean]]("debug", "static dir to serve")
    val port = SettingKey[Option[Int]]("port", "http server port")
    val extensions = SettingKey[Option[List[String]]]("extensions", "http server port")
    val runserve = taskKey[Unit]("starts http server")
    val runwatch = taskKey[Unit]("watch dist and watchTarget")
    val livereload = taskKey[Unit]("starts live reload")
    val defaultExtensions = List("js", "map", "css", "jpg", "jpeg", "png", "ico", "html")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    watchTarget := None,
    copyTo := None,
    dist := None,
    distJsFolder := None,
    port := None,
    debug := None,
    extensions := None,
    watchDist := None,
    runserve := {
      val s = streams.value
      Server.start(CustomLogger(s.log), ServerConfigs(port.value, dist.value))
    },
    runwatch := {
      val targetName = s"${name.value}-fastopt"
      val target = new File((Compile / crossTarget).value, targetName)
      val targetPath = watchTarget.value.getOrElse(target)
      val extensions_ = extensions.value.getOrElse(defaultExtensions)
      val debug_ = debug.value.getOrElse(false)
      val logger = CustomLogger(streams.value.log)

      FileWatcher.setLogger(logger)

      // set copyTo to copy destination on change target files
      copyTo.value.foreach {
        f => {
          WatcherUtil.watch(extensions_, debug_, logger, targetPath, Some(f), notify = false) // dist.value.isEmpty
        }
      }

      // set dist to copy destination on change target files
      dist.value.foreach {
        f => {
          val destination = new File(f, distJsFolder.value.getOrElse("assets/js"))
          WatcherUtil.watch(extensions_, debug_, logger, targetPath, Some(destination), notify = false)
        }
      }



      // watch dist path to notify on changes
      watchDist.value.orElse(Some(dist.value.nonEmpty)).filter(x => x).flatMap(_ => dist.value).foreach {
        distTarget => {
          val dirs = distTarget :: WatcherUtil.getAllDirs(distTarget)
          dirs.foreach {
            f => WatcherUtil.watch(extensions_, debug_, logger, f, None, notify = false)
          }
        }
      }
    },
    Compile / compile := {
      val c = (Compile / compile).value
      val s = streams.value
      val logger = CustomLogger(s.log)
      FileWatcher.setLogger(logger)
      Server.setLogger(logger)
      println(s"sessions to notify ${WsSession.count}")
      Server.notify(logger)
      c
    },
    (Global / onUnload) := {
      (Global / onUnload).value.compose { state =>
        val logger = CustomLogger(state.log)
        FileWatcher.setLogger(logger)
        Server.setLogger(logger)
        logger.info("stop server")
        FileWatcher.stop()
        logger.info("stop file watch")
        Server.stop()
        state
      }
    },
    livereload := runserve.dependsOn(Def.task(runwatch.value)).value
  )
}