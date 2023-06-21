package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.orange
import com.zegreatrob.coupling.client.components.party.CouplingLogo
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.Props
import react.dom.html.ReactHTML.span
import react.router.dom.Link
import web.cssom.ClassName
import web.cssom.px

val AboutButton by nfc<Props> {
    Link {
        to = "/about"
        tabIndex = -1
        draggable = false
        add(CouplingButton(large, orange, ClassName(""))) {
            span { +"About" }
            span {
                css { margin = 2.px }
                CouplingLogo {
                    width = 27.0
                    height = 18.0
                }
            }
        }
    }
}
