@file:JsModule("@auth0/auth0-react")

package com.zegreatrob.coupling.client.components.external.auth0.react

import react.ElementType
import react.Props
import kotlin.js.Json
import kotlin.js.Promise

external val Auth0Provider: ElementType<Auth0ProviderProps>

external interface Auth0ProviderProps : Props {
    var domain: String
    var clientId: String
    var cacheLocation: String
    var useRefreshTokens: Boolean?
    var authorizationParams: Auth0AuthorizationParams
}

external interface Auth0AuthorizationParams {
    @JsName("redirect_uri")
    var redirectUri: String
    var audience: String
    var scope: String
}

external fun useAuth0(): Auth0Hook

external interface Auth0Hook {
    fun loginWithRedirect()
    val user: Auth0User?
    val isLoading: Boolean?
    val isAuthenticated: Boolean?
    val error: Throwable?
    fun getAccessTokenSilently(options: Json): Promise<String>
    fun getIdTokenClaims(): Promise<Json>
    fun logout(json: Auth0LogoutStructure = definedExternally)
}

external interface Auth0LogoutStructure {
    var clientId: String
    var logoutParams: Auth0LogoutParams
}

external interface Auth0LogoutParams {
    var returnTo: String
}

external interface Auth0User {
    val name: String
    val email: String
}
