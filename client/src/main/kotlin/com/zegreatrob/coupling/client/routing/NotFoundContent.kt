package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.minreact.add
import react.FC
import react.Props
import react.router.dom.Link

val notFoundContent = FC<Props> {
    Link {
        this.to = "/"
        add(CouplingButton()) {
            +"Looks like an error happened. Click this to go back home."
        }
    }
}
