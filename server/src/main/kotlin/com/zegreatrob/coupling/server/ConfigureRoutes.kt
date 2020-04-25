package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.env
import com.zegreatrob.coupling.server.express.isInDevMode
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express_graphql.graphqlHTTP
import com.zegreatrob.coupling.server.external.expressws.ExpressWs
import com.zegreatrob.coupling.server.external.graphqlSchema
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.route.WS
import com.zegreatrob.coupling.server.route.WebSocketServer
import com.zegreatrob.coupling.server.route.tribeListRouter
import com.zegreatrob.coupling.server.route.websocketRoute
import kotlin.js.json

fun ExpressWs.routes() = with(app) { routes(getWss()) }

fun Express.routes(webSocketServer: WebSocketServer) {
    authRoutes()
    get("/", indexRoute())
    get("/api/logout") { request, response, _ -> request.logout();response.send("ok") }
    all("/api/*", apiGuard())
    use("/api/tribes", tribeListRouter)
    use("/api/graphql", graphqlHTTP(json("schema" to graphqlSchema(), "graphiql" to true)))
    ws("/api/:tribeId/pairAssignments/current", websocketRoute(webSocketServer))
    ws("*") { ws, _ -> ws.close() }
    get("*", indexRoute())
}

private fun websocketRoute(webSocketServer: WebSocketServer): (WS, Request) -> Unit = { connection, request ->
    websocketRoute(connection, request, webSocketServer)
}

private fun Express.authRoutes() {
    post("/auth/google-token", authenticateCustomGoogle(), send200())
    get("/microsoft-login", authenticateAzure())
    post("/auth/signin-microsoft", authenticateAzureWithFailure(), redirectToRoot())
    if (isInDevMode)
        get("/test-login", authenticateLocal())
}

private fun authenticateLocal() = passport.authenticate(
    "local",
    json("successRedirect" to "/", "failureRedirect" to "/login")
)

private fun send200(): Handler = { _, response, _ -> response.sendStatus(200) }

private fun authenticateCustomGoogle() = passport.authenticate("custom")

private fun authenticateAzure() = passport.authenticate("azuread-openidconnect")

private fun authenticateAzureWithFailure() = passport.authenticate(
    "azuread-openidconnect",
    json("failureRedirect" to "/")
)

private fun redirectToRoot(): Handler = { _, response, _ -> response.redirect("/") }

private fun Express.indexRoute(): Handler = { request, response, _ ->
    response.render(
        "index",
        json(
            "title" to "Coupling",
            "buildDate" to Config.buildDate,
            "gitRev" to Config.gitRev,
            "googleClientId" to Config.googleClientID,
            "expressEnv" to env,
            "isAuthenticated" to request.isAuthenticated()
        )
    )
}

private fun apiGuard(): Handler = { request, response, next ->
    request.statsdkey = listOf("http", request.method.toLowerCase(), request.path).joinToString(".")

    if (!request.isAuthenticated()) {
        handleNotAuthenticated(request, response)
    } else {
        commandDispatcher(request.user, "${request.method} ${request.path}", request.traceId)
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

