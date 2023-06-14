package br.com.mobilemind.livereload.plugin

import sbt.Logger
import sbt.internal.util.ManagedLogger

import java.time.LocalDateTime

object CustomLogger{
  def apply(logger: ManagedLogger) = new CustomLogger(Some(logger))
}

class CustomLogger(logger: Option[ManagedLogger]) {

  sealed class LogLevel(val level: String)

  case class LevelInfo() extends LogLevel("info")

  case class LevelError() extends LogLevel("error")

  case class LevelDebug() extends LogLevel("error")

  def debug(s: String): Unit = log(LevelDebug(), s)

  def info(s: String): Unit = log(LevelInfo(), s)

  def error(s: String): Unit = log(LevelError(), s)

  private def log(level: LogLevel, text: String): Unit = logger.foreach(_.info(s"[${level.level}][LiverReload]: ${text}"))
}
