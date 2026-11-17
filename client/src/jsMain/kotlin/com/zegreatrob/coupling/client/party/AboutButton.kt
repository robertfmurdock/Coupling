package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.client.components.party.CouplingLogo
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.span
import web.cssom.px

val AboutButton by nfc<Props> {
    CouplingLink {
        to = "/about"
        sizeRuleSet = large
        colorRuleSet = orange
        span { +"About" }
        span {
            css { margin = 2.px }
            CouplingLogo(width = 27.0, height = 18.0)
        }
    }
}
