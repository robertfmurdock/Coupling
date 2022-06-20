package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.minreact.add
import csstype.ClassName
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val AboutButton = FC<Props> {
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
