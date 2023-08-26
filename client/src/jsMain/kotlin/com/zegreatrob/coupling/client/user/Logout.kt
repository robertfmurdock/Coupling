package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.components.external.auth0.react.Auth0LogoutParams
import com.zegreatrob.coupling.client.components.external.auth0.react.Auth0LogoutStructure
import com.zegreatrob.coupling.client.components.external.auth0.react.useAuth0Data
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.minreact.nfc
import kotlinx.browser.window
import org.w3c.dom.get
import react.dom.html.ReactHTML.div

val Logout by nfc<PageProps> {
    val auth0Data = useAuth0Data()
    auth0Data.logout(
        Auth0LogoutStructure(
            logoutParams = Auth0LogoutParams(returnTo = "${window.location.origin}${window["basename"]}"),
        ),
    )
    div { }
}
