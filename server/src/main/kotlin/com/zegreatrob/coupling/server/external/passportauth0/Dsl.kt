package com.zegreatrob.coupling.server.external.passportauth0

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.json

fun auth0Strategy() = Auth0Strategy(
    json(
        "domain" to Config.AUTH0_DOMAIN,
        "clientID" to Config.AUTH0_CLIENT_ID,
        "clientSecret" to Config.AUTH0_CLIENT_SECRET,
        "callbackURL" to Config.AUTH0_CALLBACK_URL,
        "passReqToCallback" to true,
    )
) { request, _, _, _, profile, done ->
    request.scope.async(done, suspend {
        findOrCreateUser(profile, request)
    })
}

private suspend fun findOrCreateUser(
    profile: Auth0Profile,
    request: Request
) = profile.emails.firstOrNull()?.value?.let {
    UserDataService.findOrCreateUser(it, request.traceId)
} ?: throw Exception("Auth succeeded but no email found")

