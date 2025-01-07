# ScalsJS Live Reload Plugin

## Use g8 template

```shell
sbt new mobilemindtec/scalajs-livereload.g8 
```

## Options

### Tasks

* `livereload` use this task to start plugin

* `livereloadServe` start only server

* `livereloadWatch` start only watcher


### Configs

##### Server configs

```sbt 
livereloadDebug := Some(true)
livereloadServerPort := Some(10101)
livereloadExtensions := Some(List("js", "map", "css", "jpg", "jpeg", "png", "ico", "html"))
```

#### Use plugin to copy files to external location.

This plugin can be used to copy the generated JS to an external project. 
For example a golang or node application. In this case we need to specify the location where the js will be copied and the folder we want to monitor changes for autoreload. 

Config example:

```shell
my-go-app/
  public/
    js/
my-scalajs-app/
  src/
```

```sbt
.settings(
    livereloadWatchTarget := Some(baseDirectory.value / ".." / "my-go-app" / "public")
    livereloadCopyJSTo := Some(baseDirectory.value / ".." / "my-go-app" / "public" / "js")
    copyFullJS := livereloadCopyJSTo.value.get
)
.settings(
  Seq(fullOptJS, fastOptJS)
    .map(task => (Compile / crossTarget) := livereloadCopyJSTo.value.get)
)
```

#### Use plugin to serve files

We can use the plugin in a quick project, or single page application, where everything we need is inside the public folder.

Config:
```sbt 
livereloadPublic := Some(baseDirectory.value / "public")
copyFullTarget := baseDirectory.value / "public" / "assets" / "js" / "main.js"
livereloadPublicJS := Some(baseDirectory.value / "public" / "assets" / "js")
livereloadWatchPublic := Some(true)
```

## Example

### plugins.sbt

```sbt
addSbtPlugin("br.com.mobilemind" % "livereload" % "0.2.5")
```

### Public folder

```shell
app/
    src/
    public/
      index.html
      assets/
        js/
```

### build.sbt
```sbt

ThisBuild / name := "example"
ThisBuild / scalaVersion := "3.4.0"

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


## Publish

local

```
sbt:appjs> sbt publishM2
```

maven central

```
sbt sonatypeBundleRelease
```