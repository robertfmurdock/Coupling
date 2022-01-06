package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Next
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jwt.jwt
import com.zegreatrob.coupling.server.external.express_graphql.graphqlHTTP
import com.zegreatrob.coupling.server.external.jwksrsa.expressJwtSecret
import com.zegreatrob.coupling.server.external.passport.passport
import com.zegreatrob.coupling.server.graphql.couplingSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.js.json

fun Express.routes() {
//    authRoutes()
//    use(
//        expressOpenIdConnect(
//            json(
//                "authRequired" to false,
//                "auth0Logout" to true,
//                "baseURL" to "${Config.publicUrl}${Config.clientBasename}",
//                "clientID" to Config.AUTH0_CLIENT_ID,
//                "issuerBaseURL" to "https://${Config.AUTH0_DOMAIN}",
//                "secret" to Config.AUTH0_CLIENT_SECRET,
//                "routes" to json(
//                    "login" to "/auth0-login",
//                    "logout" to "/api/logout",
//                    "callback" to "/auth/signin-auth0"
//                )
//            ),
//        )
//    )


    get("/", indexRoute())
//    get("/api/logout") { request, response, _ -> request.logout();response.send("ok") }

    use(jwtMiddleware())

    use { request, _, next ->
        val userProfile = request.auth
        if (userProfile != null) {
            UserDataService.deserializeUser(
                request, "${userProfile["email"]}"
            ) { error, user ->
                request.asDynamic()["user"] = user

                println("user is ${request.user}")
                next()
            }
        } else {
            next()
        }
    }
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to couplingSchema(), "graphiql" to true)))
    get("*", indexRoute())
}

private fun jwtMiddleware(): Handler {
    return jwt(
        json(
            "secret" to expressJwtSecret(
                json(
                    "cache" to true,
                    "rateLimit" to true,
                    "jwksRequestsPerMinute" to 5,
                    "jwksUri" to "https://${Config.AUTH0_DOMAIN}/.well-known/jwks.json"
                )
            ),
            "issuer" to "https://${Config.AUTH0_DOMAIN}/",
            "algorithms" to arrayOf("RS256"),
            "audience" to "https://localhost/api",
            "requestProperty" to "auth",
        )
    )
}

private fun Express.authRoutes() {
    get("/auth0-login", authenticateAuth0())
    get("/auth/signin-auth0", auth0Callback())

    if (Config.TEST_LOGIN_ENABLED)
        get("/test-login", authenticateLocal())
}

private fun auth0Callback(): Handler = { request, response, next ->
    passport.authenticate("auth0")(request, response) {
        redirectToRoot()(request, response, next)
    }
}

private fun authenticateLocal(): Handler = { request, response, _ ->
    passport.authenticate(
        "local",
        json("failureRedirect" to "${Config.clientBasename}/login")
    )(request, response) {
        response.send("<html><body>OK</body></html>")
        response.sendStatus(200)
    }
}

private fun authenticateAuth0() = passport.authenticate("auth0", json("scope" to "openid email profile"))

private fun redirectToRoot(): Handler = { _, response, _ -> response.redirect("/") }

fun apiGuard(): Handler = { request, response, next ->
    request.statsdkey = listOf("http", request.method.lowercase(), request.path).joinToString(".")
    request.scope.launch(block = setupDispatcher(request, next))
}

private fun setupDispatcher(request: Request, next: Next): suspend CoroutineScope.() -> Unit = {
    val commandDispatcher = request.commandDispatcher()
    request.asDynamic().commandDispatcher = commandDispatcher
    next()
}

private suspend fun Request.commandDispatcher() =
    com.zegreatrob.coupling.server.commandDispatcher(user, scope, traceId)
