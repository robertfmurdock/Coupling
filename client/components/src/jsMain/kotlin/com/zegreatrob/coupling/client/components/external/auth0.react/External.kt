@file:JsModule("@auth0/auth0-react")

package com.zegreatrob.coupling.client.components.external.auth0.react

import react.ElementType
import react.PropsWithChildren
import kotlin.js.Json
import kotlin.js.Promise

external val Auth0Provider: ElementType<Auth0ProviderProps>

external interface Auth0ProviderProps : PropsWithChildren {
    var domain: String
    var clientId: String
    var cacheLocation: String
    var useRefreshTokens: Boolean?
    var skipRedirectCallback: Boolean?

    var authorizationParams: Auth0AuthorizationParams
}

sealed external interface Auth0AuthorizationParams {
    @JsName("redirect_uri")
    var redirectUri: String
    var audience: String
    var scope: String
}

external fun useAuth0(): Auth0Hook

external interface Auth0Hook {
    fun loginWithRedirect(options: RedirectLoginOptions = definedExternally)
    val user: Auth0User?
    val isLoading: Boolean?
    val isAuthenticated: Boolean?
    val error: Throwable?
    fun getAccessTokenSilently(options: Json): Promise<String>
    fun getIdTokenClaims(): Promise<Json>
    fun logout(json: Auth0LogoutStructure = definedExternally)
}

sealed external interface RedirectLoginOptions {
    var appState: TAppState
}

sealed external interface TAppState {
    var returnTo: String
}

sealed external interface Auth0LogoutStructure {
    var clientId: String?
    var logoutParams: Auth0LogoutParams?
}

sealed external interface Auth0LogoutParams {
    var returnTo: String
}

external interface Auth0User {
    val name: String
    val email: String
}
