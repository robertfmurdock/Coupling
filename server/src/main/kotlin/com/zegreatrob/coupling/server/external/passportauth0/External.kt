@file:Suppress("PropertyName", "unused")

package com.zegreatrob.coupling.server.external.passportauth0

import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json

@JsModule("passport-auth0")
external class Auth0Strategy(
    options: Json,
    func: (Request, String, String, Json, Auth0Profile, (dynamic, dynamic) -> Unit) -> Unit
)


external interface Auth0Profile {
    val displayName: String
    val id: String
    val user_id: String
    val provider: String
    val name: Auth0Name
    val emails: Array<Auth0Email>
    val picture: String
    val locale: String
    val nickname: String
}

external interface Auth0Email {
    val value: String
}


external interface Auth0Name {
    val familyName: String
    val givenName: String
}

