# ScalsJS Live Reload Plugin

## Options

### Tasks

```scala
    val runserve = taskKey[Unit]("starts http server")
    val runwatch = taskKey[Unit]("watch dist and watchTarget")
    val livereload = taskKey[Unit]("starts live reload")

```

### Configs

```scala
    var watchTarget = SettingKey[Option[File]]("watchTarget", "js target to watch")
    val copyTo = SettingKey[Option[File]]("copyTo", "destination to copy change files, default is `dist/distJsFolder`")
    val dist = SettingKey[Option[File]]("dist", "static dir to serve")
    val distJsFolder = SettingKey[Option[String]]("distJsFolder", "dist js folder, default assets/js")
    val watchDist = SettingKey[Option[Boolean]]("watchDist", "should watch dist folder, default is true")
    val debug = SettingKey[Option[Boolean]]("debug", "debug mode")
    val port = SettingKey[Option[Int]]("port", "http server port, default is 10101")
    val extensions = SettingKey[Option[List[String]]]("extensions", "watch extensions, default is js,map,css,jpg,jpeg,png,ico,html")   
```

## Example

### plugins.sbt
``` scala
addSbtPlugin("br.com.mobilemind" % "livereload" % "0.2.0")
```

### build.sbt
```scala

ThisBuild / name := "example"
ThisBuild / scalaVersion := "3.3.0"

resolvers += "mobilemind" at "https://maven.pkg.github.com/mobilemindtec/m2/"

lazy val app = (project in file("."))
	.enablePlugins(ScalaJSPlugin, LiveReloadJSPlugin)
	.enablePlugins()
	.settings(
		name := "example",
		scalaJSUseMainModuleInitializer := true,	
		dist := Some(baseDirectory.value / "public")
	)
	
```

## Usage

- Add on HTML page
```
<script type="text/javascript" src="http://localhost:10101/js/livereload.js"></script>
```

- In sbt console execute

```
sbt:appjs> livereload
```

- Run ~fastLinkJS to compile scalajs files.

```
sbt:appjs> ~fastLinkJS
```

- Done, the HTML page will be reloaded.

### Test project

- On appjs folder, run sbt

```
sbt:appjs> livereload
```

- Open test html on http://localhost:10101/sample/index.html.

```
sbt:appjs> ~fastLinkJS
```

- Change `Main.scala` and save to HTML reload.