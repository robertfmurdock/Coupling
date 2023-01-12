package com.zegreatrob.coupling.components.external.auth0.react

import kotlin.js.Json

data class AuthHookData(
    val user: Auth0User?,
    val authenticated: Boolean,
    val loading: Boolean,
    val error: Throwable?,
    val loginWithRedirect: () -> Unit,
    val getIdTokenClaims: suspend () -> String,
    val logout: (Json) -> Unit,
    val getAccessTokenSilently: suspend () -> String
)
