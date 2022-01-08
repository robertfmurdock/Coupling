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
import com.zegreatrob.coupling.server.graphql.couplingSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    use(jwtMiddleware())
    use(userLoadingMiddleware())
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to couplingSchema(), "graphiql" to true)))
    get("*", indexRoute())
}

fun userLoadingMiddleware(): Handler = { request, _, next ->
    val auth = request.auth
    if (auth != null) {
        UserDataService.deserializeUser(request, "${auth["email"]}") { _, user ->
            request.asDynamic()["user"] = user
            request.asDynamic()["isAuthenticated"] = true
            next()
        }
    } else {
        next()
    }
}

fun jwtMiddleware(getToken: ((Request) -> dynamic)? = null): Handler = jwt(
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
        "requestProperty" to "auth",
        "credentialsRequired" to false,
    ).let {
        if (getToken == null) {
            it
        } else {
            it.add(json("getToken" to getToken))
        }
    }
)

fun apiGuard(): Handler = { request, response, next ->
    request.statsdkey = listOf("http", request.method.lowercase(), request.path).joinToString(".")

    if (!request.isAuthenticated) {
        response.sendStatus(401)
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
