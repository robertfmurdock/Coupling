package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.client.components.party.CouplingLogo
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.span
import tanstack.react.router.Link
import tanstack.router.core.RoutePath
import web.cssom.px

val AboutButton by nfc<Props> {
    Link {
        to = RoutePath("/about")
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = orange
            span { +"About" }
            span {
                css { margin = 2.px }
                CouplingLogo(width = 27.0, height = 18.0)
            }
        }
    }
}
