package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.isInDevMode
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.external.express_graphql.graphqlHTTP
import com.zegreatrob.coupling.server.external.expressws.ExpressWs
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.graphql.couplingSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.js.json

fun ExpressWs.routes() = with(app) { routes(getWss()) }

fun Express.routes(webSocketServer: WebSocketServer) {
    authRoutes()
    get("/", indexRoute())
    get("/api/logout") { request, response, _ -> request.logout();response.send("ok") }
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to couplingSchema(), "graphiql" to true)))
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
    get("/auth0-login", authenticateAuth0())
    get("/auth/signin-auth0", auth0Callback())

    if (isInDevMode)
        get("/test-login", authenticateLocal())
}

private fun auth0Callback(): Handler = { request, response, next ->
    passport.authenticate("auth0")(request, response) {
        redirectToRoot()(request, response, next)
    }
}

private fun authenticateLocal() = passport.authenticate(
    "local",
    json("successRedirect" to "/", "failureRedirect" to "/login")
)

private fun send200(): Handler = { _, response, _ -> response.sendStatus(200) }

private fun authenticateCustomGoogle() = passport.authenticate("custom")

private fun authenticateAuth0() = passport.authenticate("auth0", json("scope" to "openid email profile"))

private fun authenticateAzure() = passport.authenticate("azuread-openidconnect")

private fun authenticateAzureWithFailure() = passport.authenticate(
    "azuread-openidconnect",
    json("failureRedirect" to "/")
)

private fun redirectToRoot(): Handler = { _, response, _ -> response.redirect("/") }

private fun apiGuard(): Handler = { request, response, next ->
    request.statsdkey = listOf("http", request.method.lowercase(), request.path).joinToString(".")

    if (!request.isAuthenticated()) {
        handleNotAuthenticated(request, response)
    } else {
        request.scope.launch(block = setupDispatcher(request, next))
    }
}

private fun setupDispatcher(request: Request, next: Next): suspend CoroutineScope.() -> Unit = {
    val commandDispatcher = request.commandDispatcher()
    request.asDynamic().commandDispatcher = commandDispatcher
    next()
}

private suspend fun Request.commandDispatcher() =
    com.zegreatrob.coupling.server.commandDispatcher(user, scope, traceId)

private fun handleNotAuthenticated(request: Request, response: Response) = if (request.isWebsocketConnection()) {
    request.close()
} else {
    response.sendStatus(401)
}

private fun Request.isWebsocketConnection() = originalUrl?.contains(".websocket") == true
