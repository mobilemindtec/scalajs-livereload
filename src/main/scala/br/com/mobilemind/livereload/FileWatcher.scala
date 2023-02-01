package br.com.mobilemind.livereload

import java.io.{File, FileWriter}
import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}
import java.nio.file.{FileSystems, Files, Path, Paths, StandardCopyOption, WatchEvent}
import java.util.{Timer, TimerTask}

object FileWatcher {

	private var timer: Option[Timer] = None

	def start(dist: File, target: File) = {
		val timer = new Timer
		timer.schedule(new TimerTask {
			override def run(): Unit = {
				watch(dist, target)
			}
		}, 0)
	}

	def stop() = timer.map(_.cancel())

	def watch(dist: File, target: File) = {

		val watcher = FileSystems.getDefault.newWatchService
		Paths.get(dist.getAbsolutePath).register(watcher,
			ENTRY_CREATE,
			ENTRY_DELETE,
			ENTRY_MODIFY);

		//val writer = new FileWriter(new File(target.getAbsolutePath, "file.text"), true)
		while (true) {
			val k = watcher.take()
			val events = k.pollEvents().toArray
			for (event <- events) {
				val ev = event.asInstanceOf[WatchEvent[Path]]
				val fname = ev.context().toFile.getName

				if(fname.endsWith(".js") || fname.endsWith(".map")) {
					val from = Paths.get(dist.getAbsolutePath, fname)
					val to = Paths.get(target.getAbsolutePath, fname)
					//log.info(s"copy from $from to $to")
					Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
				}


				//writer.write(fname)
				//writer.flush()
			}
			k.reset()
		}
	}
}
