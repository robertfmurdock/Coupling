package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.external.express.Express
import kotlin.js.Json
import kotlin.js.json


@JsModule("compression")
@JsNonModule
external fun compression(): dynamic

@JsModule("express-statsd")
@JsNonModule
external fun statsd(config: Json): dynamic

fun configureExpress(app: Express, userDataService: Json) {
    app.use(compression())
    app.use(statsd(json("host" to "statsd", "port" to 8125)))
    app.set("port", Config.port)
    app.set("views", arrayOf(resourcePath("public"), resourcePath("views")))
    app.set("view engine", "pug")
}

fun resourcePath(directory: String): String {
    return "$directory"
}
