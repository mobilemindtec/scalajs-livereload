package br.com.mobilemind.livereload.core

import br.com.mobilemind.livereload.plugin.CustomLogger
import cask.Ws
import cask.endpoints.WsChannelActor
import cask.model.Response
import io.undertow.{Undertow, UndertowOptions}
import upickle.default.*

import java.io.{BufferedReader, File, FileReader}
import scala.collection.JavaConverters.*
import scala.collection.mutable.ListBuffer


case class ServerConfigs(port: Option[Int]  = None, www: Option[File] = None)

class myStaticResources(path: String, headers: Seq[(String, String)]) extends
	cask.staticResources(path, classOf[cask.staticResources].getClassLoader, headers)

case class Asset(data: String, contentType: String, status: Int = 200)

object WsSession {

	private var sessions = ListBuffer[WsChannelActor]()

	def count = sessions.length

	def newSession(id: String)(implicit ctx: castor.Context, log: cask.Logger): cask.WsHandler = {
		cask.WsHandler { channel =>
			sessions.append(channel)
			val actor = newSession(id, channel)
			channel.send(createEvent("alive"))
			actor
		}
	}

	private def newSession(id: String, channel: WsChannelActor)(implicit ctx: castor.Context, log: cask.Logger): cask.WsActor = {
			cask.WsActor {
				case cask.Ws.Text("") => channel.send(cask.Ws.Close())
				case cask.Ws.Text(data) =>
					channel.send(cask.Ws.Text(id + " " + data))
				case cask.Ws.ChannelClosed() =>
					sessions -= channel
					//if (idx > -1) sessions.remove(idx)
			}
	}


	def notify(logger: CustomLogger): Unit = {
		logger.info(s"[info] LiveReload: ${sessions.size} active sessions")
		sessions.foreach(c => c.send(createEvent("reload")))
	}

	private def createEvent(s: String) =
		Ws.Text(write(Map("event" -> s)))

	def stop(): Unit = {
		sessions.clear()
	}
}

class AppController(dist: => Option[File])(implicit ctx: castor.Context,
																					log: cask.Logger) extends cask.MainRoutes {

	private def readIndexHtml(path: File): Option[String] = {
		val indexFile = new File(path, s"index.html")
		if(indexFile.exists()) {
			val reader = new BufferedReader(new FileReader(indexFile))
			try{
				val lines = reader.lines().toList.asScala
				Some(lines.mkString("\n"))
			}finally {
				reader.close()
			}
		} else None
	}

	private def readAsset(assetPath: File, assetParts: String): Asset = {
		val file = new File(assetPath, s"/assets/${assetParts}")
		if (file.exists()) {
			val fileName = file.getName
			val ext = fileName.split("\\.").toList.last
			val contentType = MimeTypes.values.getOrElse(s".$ext", MimeTypes.defaultMimeType)
			val reader = new BufferedReader(new FileReader(file))
			try {
				val data = reader.lines().toList.asScala.mkString("\n")
				Asset(data, contentType)
			} finally reader.close()

		} else Asset(s"file ${file.getAbsolutePath} not found", "text/plain", 404)
	}

	@cask.get("/healthcheck")
	def healthcheck() = s"live reload is alive"

	@cask.get("/")
	def index(): Response[String] = {
		val resp = dist match {
			case Some(file) => readIndexHtml(file).getOrElse("index.html not fond")
			case _ => "live reload is alive"
		}
		cask.Response(resp, 200, Seq("Content-Type" -> "text/html"))
	}

	@cask.get("/dist")
	def getDist() = {
		dist.map(_.getAbsolutePath).getOrElse("empty")
	}

	@cask.get("/assets", subpath = true)
	def assets(req: cask.Request) = {
		dist match {
			case Some(f) => {
				val assetParts = req.remainingPathSegments.mkString("/")
				val Asset(data, contentType, status)= readAsset(f, assetParts)
				cask.Response(data, status, Seq("Content-Type" -> contentType))
			}
			case _ => cask.Response("not found", 404)
		}
	}

	@myStaticResources("/demo", headers = Seq("Content-Type" -> "text/html"))
	def demo() = "public/html/index.html"

	@myStaticResources("/js", headers = Seq("Content-Type" -> "text/javascript"))
	def staticJs() = "public/js"

	@cask.websocket("/ws")
	def ws(): cask.WebsocketResult = WsSession.newSession("userName")

	initialize()
}

object Server extends cask.Main {

	private var logger: Option[CustomLogger] = None
	private var server : Option[Undertow] = None
	private var configs: Option[ServerConfigs] = None

	private def getDist = configs.getOrElse(ServerConfigs()).www

	val allRoutes: Seq[AppController] = Seq(new AppController(getDist))

	def setLogger(l: CustomLogger): Unit = logger = Some(l)

	def start(l: CustomLogger, conf: ServerConfigs): Unit = {
		logger = Some(l)
		configs = Some(conf)
		main(Array())		
	}

	def stop(): Unit = {
		logger.foreach(_.info(s"stop [${WsSession.count}] WS sessions"))
		WsSession.stop()
		server.foreach(_.stop())
	}

	override def main(args: Array[String]): Unit = {
		val port = configs.getOrElse(ServerConfigs()).port.getOrElse(10101)
		val srv = Undertow.builder
			.addHttpListener(port, "0.0.0.0")
			// increase io thread count as per https://github.com/TechEmpower/FrameworkBenchmarks/pull/4008
			.setIoThreads(Runtime.getRuntime.availableProcessors() * 2)
			// In HTTP/1.1, connections are persistent unless declared otherwise.
			// Adding a "Connection: keep-alive" header to every response would only
			// add useless bytes.
			.setServerOption[java.lang.Boolean](UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
			.setHandler(defaultHandler)
			.build
		srv.start()
		server = Some(srv)
		logger.foreach(_.info(s"Start server on http://localhost:${port}"))
	}

	def notify(l: CustomLogger): Unit = {
		WsSession.notify(l)
	}
}