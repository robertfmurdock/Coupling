package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.ClassName

val PartySelectButton by nfc<Props> {
    Link {
        to = "/parties/"
        tabIndex = -1
        draggable = false
        add(CouplingButton(large)) {
            i { className = ClassName("fa fa-arrow-circle-up") }
            span { +"Party select" }
        }
    }
}
