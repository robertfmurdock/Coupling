package com.zegreatrob.coupling.client.external.reactfliptoolkit

import kotlinext.js.jsObject
import react.RBuilder
import react.RHandler

fun RBuilder.flipper(flipKey: String, handler: RHandler<FlipperProps>) = child(Flipper, jsObject<FlipperProps> { }) {
    attrs {
        this.flipKey = flipKey
    }
    handler()
}


fun RBuilder.flipped(flipId: String?, handler: RHandler<FlippedProps>) = child(Flipped, jsObject<FlippedProps> { }) {
    attrs {
        this.flipId = flipId
    }
    handler()
}
