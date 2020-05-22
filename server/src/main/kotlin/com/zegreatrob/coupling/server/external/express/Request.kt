package com.zegreatrob.coupling.server.external.express

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.ActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlin.js.Json

external interface Request {
    val params: Json
    val body: dynamic
    val method: String
    val path: String
    val originalUrl: String?
    val url: String
    fun logout()
    fun isAuthenticated(): Boolean
    fun close()

    val commandDispatcher: ActionDispatcher
    val user: User
    val traceId: Uuid
    val scope: CoroutineScope
    var statsdkey: String?
}

fun Request.jsonBody() = body.unsafeCast<Json>()

fun Request.tribeId() = TribeId(params["tribeId"].toString())
fun Request.pinId() = params["pinId"].toString()
fun Request.playerId() = params["playerId"].toString()
fun Request.pairAssignmentDocumentId() = params["id"].toString().let(::PairAssignmentDocumentId)
