package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.Json
import kotlin.js.json


@JsModule("compression")
@JsNonModule
external fun compression(): dynamic

@JsModule("express-statsd")
@JsNonModule
external fun statsd(config: Json): dynamic

@JsModule("serve-favicon")
@JsNonModule
external fun favicon(iconPath: String): dynamic


@JsModule("on-finished")
@JsNonModule
external fun onFinished(response: Response, callback: dynamic)

@JsModule("method-override")
@JsNonModule
external fun methodOverride()

fun configureExpress(app: Express, userDataService: Json) {
    app.use(compression())
    app.use(statsd(json("host" to "statsd", "port" to 8125)))
    app.set("port", Config.port)
    app.set("views", arrayOf(resourcePath("public"), resourcePath("views")))
    app.set("view engine", "pug")
    app.use(favicon(resourcePath("public/images/favicon.ico")))

    if (Process.getEnv("DISABLE_LOGGING") == null) {
        app.use(logRequests())
    }

    app.use(urlencoded(json("extended" to true)))
    app.use(com.zegreatrob.coupling.server.external.bodyparser.json())
    app.use(methodOverride())
}

private fun logRequests() = { request: Request, response: Response, next: () -> Unit ->
    logRequestAsync(request, response) { callback -> onFinished(response, callback) }
        .also { next() }
}

fun resourcePath(directory: String): String {
    return "$directory"
}
