package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.components.external.auth0.react.useAuth0Data
import kotlinx.browser.window
import org.w3c.dom.get
import react.FC
import react.dom.html.ReactHTML.div
import kotlin.js.json

val Logout = FC<PageProps> {
    val auth0Data = useAuth0Data()
    auth0Data.logout(
        json("returnTo" to "${window.location.origin}${window["basename"]}")
    )
    div { }
}
