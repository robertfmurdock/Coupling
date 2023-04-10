package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val PartySelectButton = FC<Props> {
    Link {
        to = "/parties/"
        tabIndex = -1
        draggable = false
        add(com.zegreatrob.coupling.client.components.CouplingButton(com.zegreatrob.coupling.client.components.large)) {
            i { className = ClassName("fa fa-arrow-circle-up") }
            span { +"Party select" }
        }
    }
}
