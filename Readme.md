# ScalsJS Live Reload Plugin

## Options

### Tasks

```scala
    val livereloadServe = taskKey[Unit]("start http server")
    val livereloadWatch = taskKey[Unit]("start watcher")
    val livereload = taskKey[Unit]("start live reload")
```

### Configs

```scala
    var livereloadWatchTarget = SettingKey[Option[File]]("livereloadWatchTarget", "js target to watch")
    val livereloadCopyJSTo = SettingKey[Option[File]]("livereloadCopyJSTo", "destination to copy change files")
    val livereloadPublic = SettingKey[Option[File]]("livereloadPublic", "static dir to serve")
    val livereloadPublicJS = SettingKey[Option[String]]("livereloadPublicJS", "dist js folder, default assets/js")
    val livereloadWatchPublic = SettingKey[Option[Boolean]]("livereloadWatchPublic", "should watch dist folder, default is trus")
    val livereloadDebug = SettingKey[Option[Boolean]]("livereloadDebug", "debug mode")
    val livereloadServerPort = SettingKey[Option[Int]]("livereloadServerPort", "http server port")
    val livereloadExtensions = SettingKey[Option[List[String]]]("livereloadExtensions", "watch extensions")
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
        livereloadPublic := Some(baseDirectory.value / "public")
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