package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.minreact.child
import react.FC
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val LogoutButton = FC<Props> {
    Link {
        to = "/logout"
        tabIndex = -1
        draggable = false
        child(CouplingButton(large, red)) {
            i { className = "fa fa-sign-out-alt" }
            span { +"Sign Out" }
        }
    }
}