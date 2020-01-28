package com.zegreatrob.coupling.client.external.reactfliptoolkit

import kotlinext.js.jsObject
import react.RBuilder
import react.RHandler

fun RBuilder.flipper(
    flipKey: String,
    classes: String,
    handler: RHandler<FlipperProps>
) = child(Flipper, jsObject<FlipperProps> { }) {
    attrs {
        this.flipKey = flipKey
        this.className = classes
    }
    handler()
}


fun RBuilder.flipped(
    flipId: String?,
    inverseFlipId: String? = null,
    handler: RHandler<FlippedProps>
) = child(Flipped, jsObject<FlippedProps> { }) {
    attrs {
        this.flipId = flipId
        this.inverseFlipId = inverseFlipId
    }
    handler()
}
