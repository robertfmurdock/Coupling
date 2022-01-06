package com.zegreatrob.coupling.client.external.reactrouter

import kotlinext.js.jso
import react.RBuilder

fun RBuilder.prompt(`when`: Boolean, message: String) = child(PromptComponent, jso()) {
    attrs {
        this.`when` = `when`
        this.message = message
    }

}
