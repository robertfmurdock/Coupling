@file:JsModule("@auth0/auth0-react")

package com.zegreatrob.coupling.client.external.auth0.react

import react.ElementType
import react.Props
import kotlin.js.Json
import kotlin.js.Promise

external val Auth0Provider: ElementType<Auth0ProviderProps>

external interface Auth0ProviderProps : Props {
    var domain: String
    var clientId: String
    var redirectUri: String
}

external fun useAuth0(): Auth0Hook

external interface Auth0Hook {
    fun loginWithRedirect()
    val user: Auth0User?
    val isLoading: Boolean
    val isAuthenticated: Boolean
    val error: Throwable?
    fun getAccessTokenSilently(): Promise<String>
    fun getIdTokenClaims(): Promise<Json>
}

external interface Auth0User {
    val name: String
    val email: String
}
