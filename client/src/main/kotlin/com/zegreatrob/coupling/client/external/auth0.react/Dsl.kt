package com.zegreatrob.coupling.client.external.auth0.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions.Companion.window
import kotlinext.js.jso
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

fun RBuilder.auth0Provider(
    clientId: String,
    domain: String,
    redirectUri: String,
    audience: String,
    scope: String,
    handler: RBuilder.() -> Unit
) = child(
    Auth0Provider,
    jso {
        this.clientId = clientId
        this.domain = domain
        this.redirectUri = redirectUri
        this.cacheLocation = "localstorage"
        this.audience = audience
        this.scope = scope
    }, handler
)

fun useAuth0Data(): AuthHookData {
    val hook = useAuth0()
    return AuthHookData(
        user = if (hook.isAuthenticated == true) hook.user else null,
        authenticated = hook.isAuthenticated == true,
        loading = hook.isLoading == true,
        error = hook.error,
        loginWithRedirect = hook::loginWithRedirect,
        getIdTokenClaims = {
            if (hook.isAuthenticated == true)
                hook.getIdTokenClaims().collectRawToken()
            else
                ""
        },
        logout = hook::logout,
        getAccessTokenSilently = {
            hook.getAccessTokenSilently(
                json(
                    "audience" to "${window.location.origin}/api",
                    "scope" to "email"
                )
            ).await()
        }
    )
}

private suspend fun Promise<Json>.collectRawToken() =
    await()["__raw"].toString()

data class AuthHookData(
    val user: Auth0User?,
    val authenticated: Boolean,
    val loading: Boolean,
    val error: Throwable?,
    val loginWithRedirect: () -> Unit,
    val getIdTokenClaims: suspend () -> String,
    val logout: (Json) -> Unit,
    val getAccessTokenSilently: suspend () -> String,
)
