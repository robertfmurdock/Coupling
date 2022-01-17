package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.minreact.child
import react.FC
import react.Props
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.span
import react.router.dom.Link

val TribeSelectButton = FC<Props> {
    Link {
        to = "/tribes/"
        child(CouplingButton(large)) {
            i { className = "fa fa-arrow-circle-up" }
            span { +"Tribe select" }
        }
    }
}