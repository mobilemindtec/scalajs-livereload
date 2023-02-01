package br.com.mobilemind.livereload

import cask.Ws
import cask.endpoints.WsChannelActor
import io.undertow.{Undertow, UndertowOptions}
import sbt.Logger

import scala.collection.mutable.ListBuffer
import play.api.libs.json.Json

class myStaticResources(path: String, headers: Seq[(String, String)]) extends
	cask.staticResources(path, classOf[cask.staticResources].getClassLoader, headers)

class PluginLogger(logger: Logger) {
	def info(s: String): Unit = {
		logger.info(s)
	}
}

object WsSession {

	private var sessions = ListBuffer[WsChannelActor]()

	def create(id: String)(implicit ctx: castor.Context, log: cask.Logger):
	cask.WsHandler =
		cask.WsHandler { channel =>
			sessions.append(channel)
			val actor = create(id, channel)
			channel.send(createEvent("alive"))
			actor
		}

	private def create(id: String, channel: WsChannelActor)(implicit ctx: castor.Context, log: cask.Logger): cask.WsActor =
			cask.WsActor {
				case cask.Ws.Text("") => channel.send(cask.Ws.Close())
				case cask.Ws.Text(data) =>
					channel.send(cask.Ws.Text(id + " " + data))
				case cask.Ws.ChannelClosed() =>
					sessions.clear()
					//if (idx > -1) sessions.remove(idx)
			}


	def notify(logger: PluginLogger) = {
		logger.info(s"LiveReloadPlugin: ${sessions.size} active sessions")
		sessions.foreach(c => c.send(createEvent("reload")))
	}

	def createEvent(s: String) =
		Ws.Text(Json.obj("event" -> s).toString)

}

case class AppController()(implicit ctx: castor.Context,
                           log: cask.Logger) extends cask.MainRoutes {

	@cask.get("/")
	def index() =
		"live reload is alive"

	@myStaticResources("/sample", headers = Seq("Content-Type" -> "text/html"))
	def staticHtml() = "public/html"

	@myStaticResources("/js", headers = Seq("Content-Type" -> "text/javascript"))
	def staticJs() = "public/js"

	@cask.websocket("/ws")
	def ws(): cask.WebsocketResult = {
		WsSession.create("userName")
	}

	initialize()
}

object Server extends cask.Main {

	private var logger: PluginLogger = null
	private var server : Option[Undertow] = None

	val allRoutes = Seq(
		AppController()
	)

	def start(l: PluginLogger) = {
		logger = l
		main(Array())
	}

	def stop() = server.map(_.stop())

	override def main(args: Array[String]): Unit = {
		val srv = Undertow.builder
			.addHttpListener(10101, "0.0.0.0")
			// increase io thread count as per https://github.com/TechEmpower/FrameworkBenchmarks/pull/4008
			.setIoThreads(Runtime.getRuntime().availableProcessors() * 2)
			// In HTTP/1.1, connections are persistent unless declared otherwise.
			// Adding a "Connection: keep-alive" header to every response would only
			// add useless bytes.
			.setServerOption[java.lang.Boolean](UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
			.setHandler(defaultHandler)
			.build
		srv.start()
		server = Some(srv)
		logger.info("LiveReloadPlugin: Start ws watcher...")
	}

	def notify(l: PluginLogger) = WsSession.notify(l)
}