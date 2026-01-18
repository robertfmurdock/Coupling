package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.minreact.nfc
import react.Props
import tanstack.react.router.Link
import tanstack.router.core.RoutePath

val notFoundContent by nfc<Props> {
    Link {
        this.to = RoutePath("/welcome")
        CouplingButton {
            +"Looks like an error happened. Click this to go back home."
        }
    }
}
