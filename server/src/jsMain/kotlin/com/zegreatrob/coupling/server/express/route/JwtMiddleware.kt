package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jwt.expressjwt
import com.zegreatrob.coupling.server.external.jwksrsa.expressJwtSecret
import kotlin.js.json

fun jwtMiddleware(getToken: ((Request) -> dynamic)? = null): Handler {
    val auth0Issuer = "https://${Config.AUTH0_DOMAIN}/"

    return expressjwt(
        json(
            "secret" to ::signingSecret,
            "issuer" to arrayOf(auth0Issuer, Config.publicUrl),
            "audience" to "${Config.publicUrl}/api",
            "algorithms" to arrayOf("RS256", "HS256"),
            "requestProperty" to "auth",
            "credentialsRequired" to false,
        ).let {
            if (getToken == null) {
                it
            } else {
                it.add(json("getToken" to getToken))
            }
        },
    )
}

private fun signingSecret(request: Request, token: dynamic): dynamic = when (token.payload.iss) {
    Config.publicUrl -> Config.secretSigningSecret
    else -> auth0Secret(request, token)
}

private fun auth0Secret(request: Request, token: Any?): dynamic = expressJwtSecret(
    json(
        "cache" to true,
        "rateLimit" to true,
        "jwksRequestsPerMinute" to 5,
        "jwksUri" to "https://${Config.AUTH0_DOMAIN}/.well-known/jwks.json",
    ),
)(request, token)
