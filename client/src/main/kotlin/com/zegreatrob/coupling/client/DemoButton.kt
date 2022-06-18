package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.lightGreen
import com.zegreatrob.minreact.create
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
        +(CouplingButton(large, lightGreen)).create {
            i { className = ClassName("fa fa-play") }
            span { +" Demo" }
        }
    }
}
