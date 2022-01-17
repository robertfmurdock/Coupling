package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.SessionConfig.animationsDisabled
import com.zegreatrob.coupling.client.external.auth0.react.auth0Provider
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions.Companion.window
import com.zegreatrob.coupling.client.routing.CouplingRouter
import org.w3c.dom.get
import react.Props
import react.fc

val RootComponent = fc<Props> {
    auth0Provider(
        clientId = "${kotlinx.browser.window["auth0ClientId"]}",
        domain = "${kotlinx.browser.window["auth0Domain"]}",
        redirectUri = "${window.location.origin}${kotlinx.browser.window["basename"]?.toString()}",
        audience = "https://${window.location.hostname}/api",
        scope = "email",
    ) {
        child(CouplingRouter(animationsDisabled))
    }
}
