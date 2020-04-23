package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.external.expressws.ExpressWs
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.route.tribeListRouter
import kotlin.js.Json
import kotlin.js.json

@JsModule("express-graphql")
@JsNonModule
private external val graphqlHTTP: (Json) -> Router

fun configureRoutes(expressWs: ExpressWs) {
    val app = expressWs.app

    val expressEnv = configAuthRoutes(app)

    val indexRoute = configRoutes(expressEnv, app)

    app.use("/api/graphql", graphqlHTTP(json("schema" to graphqlSchema(), "graphiql" to true)))

    app.get("*", indexRoute)
}

//@JsModule("routes/graphqlSchema")
//@JsNonModule
//private external val schema: dynamic

fun graphqlSchema() = ""

@JsName("configRoutes")
fun configRoutes(expressEnv: String, app: Express): Handler {
    val indexRoute = buildIndexRoute(expressEnv)
    app.get("/", indexRoute)
    app.get("/api/logout") { request, response, _ -> request.logout();response.send("ok") }
    app.all("/api/*", apiGuard())
    app.use("/api/tribes", tribeListRouter)

    return indexRoute
}

private fun configAuthRoutes(app: Express): String {
    app.post(
        "/auth/google-token",
        passport.authenticate("custom"),
        { _, response, _ ->
            console.log("sending 200")
            response.sendStatus(200)
        })

    app.get("/microsoft-login", passport.authenticate("azuread-openidconnect"))
    app.post(
        "/auth/signin-microsoft",
        passport.authenticate("azuread-openidconnect", json("failureRedirect" to "/")),
        { _, response, _ -> response.redirect("/") }
    )

    val expressEnv = app.get("env").unsafeCast<String>()
    val isInDevMode = when (expressEnv) {
        "development" -> true
        "test" -> true
        else -> false
    }

    if (isInDevMode) {
        app.get(
            "/test-login",
            passport.authenticate("local", json("successRedirect" to "/", "failureRedirect" to "/login"))
        )
    }
    return expressEnv
}

private fun buildIndexRoute(expressEnv: String): Handler = { request, response, _ ->
    response.render(
        "index",
        json(
            "title" to "Coupling",
            "buildDate" to Config.buildDate,
            "gitRev" to Config.gitRev,
            "googleClientId" to Config.googleClientID,
            "expressEnv" to expressEnv,
            "isAuthenticated" to request.isAuthenticated()
        )
    )
}

private fun apiGuard(): Handler = { request, response, next ->
    request.statsdkey = listOf("http", request.method.toLowerCase(), request.path).joinToString(".")

    if (!request.isAuthenticated()) {
        handleNotAuthenticated(request, response)
    } else {
        commandDispatcher(request.user, "${request.method} ${request.path}", request.traceId!!)
            .then {
                request.commandDispatcher = it
                next()
            }
    }
}

private fun handleNotAuthenticated(request: Request, response: Response) =
    if (request.originalUrl?.contains(".websocket") == true) {
        request.close()
    } else {
        response.sendStatus(401)
    }

