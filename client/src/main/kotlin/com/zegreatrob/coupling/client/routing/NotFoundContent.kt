package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import react.Props
import react.router.dom.Link

val notFoundContent by nfc<Props> {
    Link {
        this.to = "/welcome"
        add(CouplingButton()) {
            +"Looks like an error happened. Click this to go back home."
        }
    }
}
