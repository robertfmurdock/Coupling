package com.zegreatrob.coupling.client.components.party

import react.FC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.p

val NoSecretsView = FC {
    div {
        h2 { +"There are no secrets yet." }
        p { +"" }
    }
}
