package com.zegreatrob.coupling.client.components.party

import react.FC
import react.dom.html.ReactHTML

val NoSecretsView = FC {
    ReactHTML.div {
        ReactHTML.h2 { +"There are no secrets yet." }
        ReactHTML.p { +"" }
    }
}
