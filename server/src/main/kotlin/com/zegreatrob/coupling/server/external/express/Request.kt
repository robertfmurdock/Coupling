package com.zegreatrob.coupling.server.external.express

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.CommandDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.js.Json

external interface Request {
    val oidc: OIDC
    val auth: Json?
    val query: Json
    val params: Json
    val body: dynamic
    val method: String
    val path: String
    val originalUrl: String?
    val url: String
    val connectionId: String
    val event: Json?
    fun logout()
    val commandDispatcher: CommandDispatcher
    val user: User
    val traceId: Uuid
    val scope: CoroutineScope
    var statsdkey: String?
}

external interface OIDC {
    val user: Json?

    fun isAuthenticated() : Boolean
}
