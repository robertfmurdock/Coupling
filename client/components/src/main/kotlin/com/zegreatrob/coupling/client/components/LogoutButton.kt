package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import csstype.ClassName
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
        add(
            com.zegreatrob.coupling.client.components.CouplingButton(
                com.zegreatrob.coupling.client.components.large,
                com.zegreatrob.coupling.client.components.red,
            ),
        ) {
            i { className = ClassName("fa fa-sign-out-alt") }
            span { +"Sign Out" }
        }
    }
}
