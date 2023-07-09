package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.ClassName

val LogoutButton by nfc<Props> {
    Link {
        to = "/logout"
        tabIndex = -1
        draggable = false
        CouplingButton(large, red) {
            i { className = ClassName("fa fa-sign-out-alt") }
            span { +"Sign Out" }
        }
    }
}
