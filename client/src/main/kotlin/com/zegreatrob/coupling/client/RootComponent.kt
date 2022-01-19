package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.SessionConfig.animationsDisabled
import com.zegreatrob.coupling.client.external.auth0.react.Auth0Provider
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions.Companion.window
import com.zegreatrob.coupling.client.routing.CouplingRouter
import com.zegreatrob.minreact.child
import org.w3c.dom.get
import react.FC
import react.Props

val RootComponent = FC<Props> {
    Auth0Provider {
        clientId = "${kotlinx.browser.window["auth0ClientId"]}"
        domain = "${kotlinx.browser.window["auth0Domain"]}"
        redirectUri = "${window.location.origin}${kotlinx.browser.window["basename"]?.toString()}"
        cacheLocation = "localstorage"
        audience = "https://${window.location.hostname}/api"
        scope = "email"
        useRefreshTokens = true
        child(CouplingRouter(animationsDisabled))
    }
}
