@file:JsModule("google-auth-library")
@file:JsNonModule
package com.zegreatrob.coupling.server.external.googleauthlibrary

import kotlin.js.Json
import kotlin.js.Promise

external class OAuth2Client(clientID: String) {
    fun verifyIdToken(tokenStuff: Json): Promise<Ticket>
}

external interface Ticket {
    fun getPayload() : Payload

}

external interface Payload {
    val email: String
}
