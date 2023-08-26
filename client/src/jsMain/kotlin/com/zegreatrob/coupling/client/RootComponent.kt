package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.SessionConfig.animationsDisabled
import com.zegreatrob.coupling.client.components.external.auth0.react.Auth0AuthorizationParams
import com.zegreatrob.coupling.client.components.external.auth0.react.Auth0Provider
import com.zegreatrob.coupling.client.components.external.w3c.WindowFunctions.Companion.window
import com.zegreatrob.coupling.client.routing.CouplingRouter
import com.zegreatrob.minreact.nfc
import react.Props

external interface RootProps : Props {
    var clientConfig: ClientConfig
}

val RootComponent by nfc<RootProps> { props ->
    val config = props.clientConfig
    Auth0Provider {
        clientId = config.auth0ClientId
        domain = config.auth0Domain
        cacheLocation = "localstorage"
        authorizationParams = Auth0AuthorizationParams(
            redirectUri = "${window.location.origin}${config.basename}",
            audience = "https://${window.location.hostname}/api",
            scope = "email",
        )
        skipRedirectCallback = isCallbackFromOtherProvider()
        useRefreshTokens = true

        CouplingRouter(animationsDisabled, config)
    }
}

private fun isCallbackFromOtherProvider() =
    window.location.pathname.endsWith("/integration/slack/callback") ||
        window.location.pathname.endsWith("/integration/discord/callback")
