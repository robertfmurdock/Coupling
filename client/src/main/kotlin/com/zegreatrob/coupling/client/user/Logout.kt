package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.components.external.auth0.react.useAuth0Data
import js.core.jso
import kotlinx.browser.window
import org.w3c.dom.get
import react.FC
import react.dom.html.ReactHTML.div

val Logout = FC<PageProps> {
    val auth0Data = useAuth0Data()
    auth0Data.logout(
        jso {
            logoutParams = jso {
                returnTo = "${window.location.origin}${window["basename"]}"
            }
        }
    )
    div { }
}
