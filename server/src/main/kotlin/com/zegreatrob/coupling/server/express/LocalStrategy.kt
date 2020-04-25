package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.UserDataService
import kotlin.js.json

typealias LocalStrategy = com.zegreatrob.coupling.server.external.passportlocal.Strategy

fun localStrategy() = LocalStrategy(json("passReqToCallback" to true)) { request, username, _, done ->
    request.scope.async(done) { UserDataService.findOrCreate("$username._temp", request.traceId, request.scope) }
}
