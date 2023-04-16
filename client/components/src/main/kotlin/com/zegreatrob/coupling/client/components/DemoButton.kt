package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import csstype.ClassName
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val DemoButton by nfc<Props> {
    Link {
        to = "/demo"
        tabIndex = -1
        draggable = false
        add(CouplingButton(large, lightGreen)) {
            i { className = ClassName("fa fa-play") }
            span { +" Demo" }
        }
    }
}
