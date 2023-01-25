package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.SessionConfig.animationsDisabled
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions.Companion.window
import com.zegreatrob.coupling.client.routing.CouplingRouter
import com.zegreatrob.coupling.components.external.auth0.react.Auth0Provider
import com.zegreatrob.minreact.add
import js.core.jso
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
        cacheLocation = "localstorage"
        authorizationParams = jso {
            redirectUri = "${window.location.origin}${config.basename}"
            audience = "https://${window.location.hostname}/api"
            scope = "email"
        }
        useRefreshTokens = true

        add(CouplingRouter(animationsDisabled, config))
    }
}
