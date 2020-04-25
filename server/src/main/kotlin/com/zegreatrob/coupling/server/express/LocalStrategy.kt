package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.UserDataService
import kotlin.js.Promise
import kotlin.js.json

typealias LocalStrategy = com.zegreatrob.coupling.server.external.passportlocal.Strategy

fun localStrategy() = LocalStrategy(json("passReqToCallback" to true)) { request, username, _, done ->
    doneAfter(done, UserDataService.findOrCreate("$username._temp", request.traceId, request.scope))
}

private fun doneAfter(done: (dynamic, dynamic) -> Unit, promise: Promise<User>) {
    promise.then({ done(null, it) }, { done(it, null) })
}
