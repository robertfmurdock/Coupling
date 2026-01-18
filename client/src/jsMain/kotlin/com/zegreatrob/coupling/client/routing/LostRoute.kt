package com.zegreatrob.coupling.client.routing

import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import tanstack.react.router.useLocation

val LostRoute by nfc<Props> {
    val location = useLocation()
    div { +"Hmm, you seem to be lost. At ${location.pathname}" }
}
