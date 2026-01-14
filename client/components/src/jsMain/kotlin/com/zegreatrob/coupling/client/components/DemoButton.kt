package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import tanstack.react.router.Link
import tanstack.router.core.RoutePath
import web.cssom.ClassName

val DemoButton by nfc<Props> {
    Link {
        to = RoutePath("/demo")
        tabIndex = -1
        draggable = false
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = lightGreen
            i { className = ClassName("fa fa-play") }
            span { +" Demo" }
        }
    }
}
