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
    var livereloadWatchTarget = SettingKey[Option[File]]("livereloadWatchTarget", "js target to watch")
    val livereloadCopyJSTo = SettingKey[Option[File]]("livereloadCopyJSTo", "destination to copy change files")
    val livereloadPublic = SettingKey[Option[File]]("livereloadPublic", "static dir to serve")
    val livereloadPublicJS = SettingKey[Option[String]]("livereloadPublicJS", "dist js folder, default assets/js")
    val livereloadWatchPublic = SettingKey[Option[Boolean]]("livereloadWatchPublic", "should watch dist folder, default is trus")
    val livereloadDebug = SettingKey[Option[Boolean]]("livereloadDebug", "debug mode")
    val livereloadServerPort = SettingKey[Option[Int]]("livereloadServerPort", "http server port")
    val livereloadExtensions = SettingKey[Option[List[String]]]("livereloadExtensions", "watch extensions")
    val livereloadServe = taskKey[Unit]("start http server")
    val livereloadWatch = taskKey[Unit]("start watcher")
    val livereload = taskKey[Unit]("start live reload")
    val defaultExtensions = List("js", "map", "css", "jpg", "jpeg", "png", "ico", "html")
  }

  import autoImport.*

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    livereloadWatchTarget := None,
    livereloadCopyJSTo := None,
    livereloadPublic := None,
    livereloadPublicJS := None,
    livereloadServerPort := None,
    livereloadDebug := None,
    livereloadExtensions := None,
    livereloadWatchPublic := None,
    livereloadServe := {
      val s = streams.value
      Server.start(CustomLogger(s.log), ServerConfigs(livereloadServerPort.value, livereloadPublic.value))
    },
    livereloadWatch := {
      val targetName = s"${name.value}-fastopt"
      val target = new File((Compile / crossTarget).value, targetName)
      val targetPath = livereloadWatchTarget.value.getOrElse(target)
      val extensions_ = livereloadExtensions.value.getOrElse(defaultExtensions)
      val debug_ = livereloadDebug.value.getOrElse(false)
      val logger = CustomLogger(streams.value.log)

      FileWatcher.setLogger(logger)

      // set copyTo to copy destination on change target files
      livereloadCopyJSTo.value.foreach {
        f => {
          WatcherUtil.watch(extensions_, debug_, logger, targetPath, Some(f), notify = false) // dist.value.isEmpty
        }
      }

      // set dist to copy destination on change target files
      livereloadPublic.value.foreach {
        f => {
          val destination = new File(f, livereloadPublicJS.value.getOrElse("assets/js"))
          WatcherUtil.watch(extensions_, debug_, logger, targetPath, Some(destination), notify = false)
        }
      }

      // watch dist path to notify on changes
      livereloadWatchPublic.value.orElse(Some(livereloadPublic.value.nonEmpty)).filter(x => x).flatMap(_ => livereloadPublic.value).foreach {
        distTarget => {
          val dirs = distTarget :: WatcherUtil.getAllDirs(distTarget)
          dirs.foreach {
            f => WatcherUtil.watch(extensions_, debug_, logger, f, None, notify = true)
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
      val watchingDist = livereloadPublic.value.isDefined && livereloadWatchPublic.value.getOrElse(true)
      if(!watchingDist) Server.notify(logger)
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
    livereload := livereloadServe.dependsOn(Def.task(livereloadWatch.value)).value
  )
}