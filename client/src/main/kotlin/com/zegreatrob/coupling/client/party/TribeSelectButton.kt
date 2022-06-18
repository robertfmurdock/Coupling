package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.minreact.create
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val PartySelectButton = FC<Props> {
    Link {
        to = "/tribes/"
        tabIndex = -1
        draggable = false
        +CouplingButton(large).create {
            i { className = ClassName("fa fa-arrow-circle-up") }
            span { +"Party select" }
        }
    }
}
