package com.zegreatrob.coupling.client.components.external.auth0.react

data class AuthHookData(
    val user: Auth0User?,
    val authenticated: Boolean,
    val loading: Boolean,
    val error: Throwable?,
    val loginWithRedirect: (RedirectLoginOptions) -> Unit,
    val getIdTokenClaims: suspend () -> String,
    val logout: (Auth0LogoutStructure) -> Unit,
    val getAccessTokenSilently: suspend () -> String,
)
