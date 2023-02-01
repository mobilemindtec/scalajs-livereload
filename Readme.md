# ScalsJS Live Reload Plugin

This plugin does two things:

  * Copy JS somewhere after compile
  * Reaload page after compile


## Usage

- Add plugin

      .enablePlugins(LiveReloadJSPlugin)


## Live reload usage 


- Add on HTML page

      <script type="text/javascript" src="http://localhost:10101/js/livereload.js"></script>


- In sbt console execute


      sbt:appjs> livereload


- Run ~fastLinkJS to compile scalajs files.


      sbt:appjs> ~fastLinkJS

- Done, the HTML page will be reloaded.

## Copy JS usage

- Configure app name and destination of copy

      .settings(
        name := "my-app-name",
        copyTarget := Some(baseDirectory.value / ".." / "web" / "static" / "js") // copy main.js to ../web/static/js
      )

- In sbt console execute

      sbt:appjs> copyFilesJS
      
- Run ~fastLinkJS to compile scalajs files.


      sbt:appjs> ~fastLinkJS
      

- Done, the JS will be copied to copyTarget.

### Use both features

- Configure both features described above
- In sbt console execute

      sbt:appjs> livewatch
      
      
- Run ~fastLinkJS to compile scalajs files.


      sbt:appjs> ~fastLinkJS
      

- Done, the JS will be copied to copyTarget and HTML page will be reloaded.      


### Test project

- On appjs folder, run sbt


      sbt:appjs> livereload


- Open test html on http://localhost:10101/sample/index.html.


      sbt:appjs> ~fastLinkJS


- Change `Main.scala` and save to HTML reload.
