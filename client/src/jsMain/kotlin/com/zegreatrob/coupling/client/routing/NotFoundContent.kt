package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.components.CouplingLink
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.p
import web.aria.AriaRole

val notFoundContent by nfc<Props> {
    p {
        role = "alert".unsafeCast<AriaRole>()
        +"Looks like an error happened."
    }
    CouplingLink {
        to = "/welcome"
        +"Go back home"
    }
}
