package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.large
import com.zegreatrob.coupling.components.lightGreen
import com.zegreatrob.minreact.add
import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val DemoButton = FC<Props> {
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
