package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.minreact.add
import react.FC
import react.Props
import react.router.dom.Link

val notFoundContent = FC<Props> {
    Link {
        this.to = "/welcome"
        add(com.zegreatrob.coupling.client.components.CouplingButton()) {
            +"Looks like an error happened. Click this to go back home."
        }
    }
}
