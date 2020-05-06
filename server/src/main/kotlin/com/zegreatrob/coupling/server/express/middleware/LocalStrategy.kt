package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.express.async
import kotlin.js.json

typealias LocalStrategy = com.zegreatrob.coupling.server.external.passportlocal.Strategy

fun localStrategy() = LocalStrategy(json("passReqToCallback" to true)) { request, username, _, done ->
    request.scope.async(done) { UserDataService.findOrCreateUser("$username._temp", request.traceId) }
}
