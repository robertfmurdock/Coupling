package com.zegreatrob.coupling.client.external.auth0.react

import kotlinext.js.jso
import kotlinx.coroutines.await
import react.RBuilder
import kotlin.js.Json
import kotlin.js.Promise

fun RBuilder.auth0Provider(clientId: String, domain: String, redirectUri: String, handler: RBuilder.() -> Unit) = child(
    Auth0Provider,
    jso {
        this.clientId = clientId
        this.domain = domain
        this.redirectUri = redirectUri
    }, handler
)

fun useAuth0Data(): AuthHookData {
    val hook = useAuth0()
    return AuthHookData(
        user = if (hook.isAuthenticated) hook.user else null,
        authenticated = hook.isAuthenticated,
        loading = hook.isLoading,
        error = hook.error,
        loginWithRedirect = hook::loginWithRedirect,
        getIdTokenClaims = {
            if (hook.isAuthenticated)
                hook.getIdTokenClaims().collectRawToken()
            else
                ""
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
    val getIdTokenClaims: suspend () -> String
)
