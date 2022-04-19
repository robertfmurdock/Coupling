package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jwt.jwt
import com.zegreatrob.coupling.server.external.jwksrsa.expressJwtSecret
import kotlin.js.json

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
        "audience" to "${Config.publicUrl}/api",
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
