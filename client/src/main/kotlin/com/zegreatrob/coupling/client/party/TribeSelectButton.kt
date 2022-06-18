package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.minreact.add
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
        add(CouplingButton(large)) {
            i { className = ClassName("fa fa-arrow-circle-up") }
            span { +"Party select" }
        }
    }
}
