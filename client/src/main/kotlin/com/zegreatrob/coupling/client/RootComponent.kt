package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.SessionConfig.animationsDisabled
import com.zegreatrob.coupling.client.external.auth0.react.Auth0Provider
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions.Companion.window
import com.zegreatrob.coupling.client.routing.CouplingRouter
import com.zegreatrob.minreact.create
import react.FC
import react.Props

external interface RootProps : Props {
    var clientConfig: ClientConfig
}

val RootComponent = FC<RootProps> { props ->
    val config = props.clientConfig
    Auth0Provider {
        clientId = config.auth0ClientId
        domain = config.auth0Domain
        redirectUri = "${window.location.origin}${config.basename}"
        cacheLocation = "localstorage"
        audience = "https://${window.location.hostname}/api"
        scope = "email"
        useRefreshTokens = true

        +(CouplingRouter(animationsDisabled, config)).create()
    }
}
