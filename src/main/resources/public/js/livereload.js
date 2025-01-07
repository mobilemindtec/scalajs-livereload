
class LiveReload{
    static start(args) {

        var testUrl = "__RELOAD_URL__"
        var reloadUrl = "__RELOAD_URL__"

        if(!testUrl || testUrl.trim().length == 0)
            testUrl = document.href

        console.log("Live Reload: Use " + reloadUrl + " as reload URL")

        var args  = {
        	port:  __PORT__,
            debug: false,
            tryLimit: 20,
            startTimeout: 1000,
            retryTimeout: 300,
            testUrl: testUrl,
            realoadUrl: reloadUrl
        }

        if(args.debug)
            console.log("Live Reload:", JSON.stringify(args))

        var url = "ws://localhost:"+args.port+"/ws"

        let liveReload = new WebSocket(url)

        function checkServerIsUp(successCb, errorCb){
            var xmlHttp = new XMLHttpRequest();
            xmlHttp.onreadystatechange = function() {
                if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
                    successCb()
                else
                    errorCb()
                    //callback(xmlHttp.responseText);
            }
            xmlHttp.open("GET", args.testUrl, true); // true for asynchronous
            xmlHttp.send(null);
        }

        function pageReload(max){

            if(max <= 0){
                console.error("Live Reload: reload limit found")
                return
            }

            checkServerIsUp(() => {

                if(args.realoadUrl && args.realoadUrl.trim().length > 0)
                    location.href = args.realoadUrl
                else
                    location.reload()

            }, () => {
                (function (i){
                    setTimeout(() => {
                        pageReload(i)
                    }, args.retryTimeout)
                })(max-1)
            })
        }

        liveReload.onopen = function (event) {
            console.log("Live Reload: Enabled.")
        }
        liveReload.onclose = function (event) {
            console.log("Live Reload: Closed.")
        }
        liveReload.onmessage = function (event) {
            let data = JSON.parse(event.data)
            let eventKey = data.event
            if (eventKey === "ping") {
                return
            }
            if (eventKey === "reload") {
                liveReload.close()
                console.log("Live Reload: reload")
                setTimeout(() => {
                    pageReload(args.tryLimit)
                }, args.startTimeout)
            } else if (eventKey === "log") {
                let level = data.level
                let message = data.message
                if (level === "info") console.info(message)
                else if (level === "log") console.log(message)
                else if (level === "warn") console.warn(message)
                else if (level === "error") console.error(message)
                else console.log("Live Reload:", message)
            } else if(eventKey == "alive") {
            	console.log("Live Reload: Channel is alive.")
            } else {
                console.log("Live Reload: Unknown message: " + data)
            }
        }
        
    };

}

(function(){
	LiveReload.start()
}())
