package com.zegreatrob.coupling.client.components.party

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import web.cssom.ClassName

val PartySelectButton by nfc<Props> {
    CouplingLink {
        to = "/parties/"
        sizeRuleSet = large
        i { className = ClassName("fa fa-arrow-circle-up") }
        span { +"Party select" }
    }
}
