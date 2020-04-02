package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.dynamo.DynamoDbProvider
import com.zegreatrob.coupling.server.external.bodyparser.urlencoded
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.static
import kotlin.js.Json
import kotlin.js.json


@JsModule("compression")
@JsNonModule
external fun compression(): dynamic

@JsModule("express-statsd")
@JsNonModule
external fun statsd(config: Json): dynamic

@JsModule("express-session")
@JsNonModule
external fun session(config: Json): dynamic

@JsModule("express-session")
@JsNonModule
external val expressSession: dynamic

@JsModule("connect-dynamodb")
@JsNonModule
external fun connectDynamoDb(session: dynamic)

fun newDynamoDbStore(@Suppress("UNUSED_PARAMETER") config: Json): dynamic {
    @Suppress("UNUSED_VARIABLE") val store = connectDynamoDb(expressSession)
    return js("new store(config)")
}

@JsModule("connect-mongo")
@JsNonModule
external fun connectMongo(session: dynamic)

fun newMongoStore(@Suppress("UNUSED_PARAMETER") config: Json): dynamic {
    @Suppress("UNUSED_VARIABLE") val store = connectMongo(expressSession)
    return js("new store(config)")
}

@JsModule("serve-favicon")
@JsNonModule
external fun favicon(iconPath: String): dynamic

@JsModule("on-finished")
@JsNonModule
external fun onFinished(response: Response, callback: dynamic)

@JsModule("method-override")
@JsNonModule
external fun methodOverride()

@JsModule("cookie-parser")
@JsNonModule
external fun cookieParser()

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

    app.use(static(resourcePath("public"), json("extensions" to arrayOf("json"))))
    app.use(cookieParser())
    app.use(buildSessionHandler())
}

fun buildSessionHandler(): dynamic {
    return session(
        json(
            "secret" to Config.secret,
            "resave" to true,
            "saveUninitialized" to true,
            "store" to chooseStore()
        )
    )
}

fun chooseStore() = if (Process.getEnv("AWS_SECRET_ACCESS_KEY") != null || Process.getEnv("LOCAL_DYNAMO") != null) {
    newDynamoDbStore(json("client" to DynamoDbProvider.dynamoDB))
} else {
    newMongoStore(json("url" to Config.mongoUrl))
}

private fun logRequests() = { request: Request, response: Response, next: () -> Unit ->
    logRequestAsync(request, response) { callback -> onFinished(response, callback) }
        .also { next() }
}

fun resourcePath(directory: String) = "$directory"
