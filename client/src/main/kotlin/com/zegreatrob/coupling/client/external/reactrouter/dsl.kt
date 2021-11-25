package com.zegreatrob.coupling.client.external.reactrouter

import kotlinext.js.jsObject
import react.RBuilder

fun RBuilder.prompt(`when`: Boolean, message: String) = child(PromptComponent, jsObject { }) {
    attrs {
        this.`when` = `when`
        this.message = message
    }

}
