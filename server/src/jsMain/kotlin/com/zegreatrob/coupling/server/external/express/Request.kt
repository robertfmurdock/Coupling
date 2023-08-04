package com.zegreatrob.coupling.server.external.express

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.server.CommandDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.js.Json

external interface Request {
    val auth: Json?
    val isAuthenticated: Boolean?
    val query: Json
    val body: dynamic
    val method: String
    val path: String
    val originalUrl: String?
    val url: String
    val connectionId: String
    val event: Json?
    val commandDispatcher: CommandDispatcher
    val user: UserDetails
    val traceId: Uuid
    val scope: CoroutineScope
    var statsdkey: String?
    fun get(headerName: String): String?
}
