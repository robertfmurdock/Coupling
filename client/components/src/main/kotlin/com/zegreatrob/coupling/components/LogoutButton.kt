package com.zegreatrob.coupling.components

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
        add(CouplingButton(large, red)) {
            i { className = ClassName("fa fa-sign-out-alt") }
            span { +"Sign Out" }
        }
    }
}
