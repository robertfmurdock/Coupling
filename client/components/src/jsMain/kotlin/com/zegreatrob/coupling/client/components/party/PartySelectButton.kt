package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import tanstack.react.router.Link
import tanstack.router.core.RoutePath
import web.cssom.ClassName

val PartySelectButton by nfc<Props> {
    Link {
        to = RoutePath("/parties/")
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            i { className = ClassName("fa fa-arrow-circle-up") }
            span { +"Party select" }
        }
    }
}
