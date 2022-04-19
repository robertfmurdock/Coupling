package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.lightGreen
import com.zegreatrob.minreact.child
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
        child(CouplingButton(large, lightGreen, ClassName(""))) {
            i {
                this.className = ClassName("fa fa-play")
            }
            span { +" Demo" }
        }
    }
}
