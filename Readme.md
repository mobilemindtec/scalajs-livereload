# ScalsJS Live Reload Plugin

## Usage

- Add on HTML page
```
<script type="text/javascript" src="/js/livereload.js"></script>
```
- Add plugin
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