package com.zegreatrob.coupling.client.external

import kotlin.js.Promise

external val gapi: GApi

external interface GApi {

    fun load(apiName: String, callback: () -> Unit)

    val auth2: GoogleAuth2

}

external interface GoogleAuth {
    val isSignedIn: GoogleProperty<Boolean>
    val currentUser: GoogleProperty<GoogleUser>
    fun signIn(options: SignInOptions): Promise<GoogleUser>
    fun signOut(): Promise<Unit>
}

external interface SignInOptions {
    var scope: String
    var prompt: String

    @Suppress("PropertyName")
    var ux_mode: String

    @Suppress("PropertyName")
    var redirect_uri: String
}

external interface GoogleProperty<T> {
    fun get(): T
}

external interface GoogleAuth2 {
    fun init(options: GoogleAuth2Options): Promise<GoogleAuth>
}

external interface GoogleAuth2Options {
    @Suppress("PropertyName")
    var client_id: dynamic
}

external interface GoogleUser {
    fun getAuthResponse(): AuthResponse
}

external interface AuthResponse {
    @Suppress("PropertyName")
    val id_token: String
}