package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML
import react.router.dom.Link
import web.cssom.ClassName

val LogoutButton by nfc<Props> {
    Link {
        to = "/logout"
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = red
            ReactHTML.i { className = ClassName("fa fa-sign-out-alt") }
            ReactHTML.span { +"Sign Out" }
        }
    }
}
