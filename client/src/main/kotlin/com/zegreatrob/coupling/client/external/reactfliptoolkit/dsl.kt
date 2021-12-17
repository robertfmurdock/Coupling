package com.zegreatrob.coupling.client.external.reactfliptoolkit

import kotlinext.js.jso
import react.RBuilder
import react.RHandler

fun RBuilder.flipper(
    flipKey: String,
    classes: String = "",
    handler: RHandler<FlippeProps>
) = child(Flipper, jso()) {
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
) = child(Flipped, jso()) {
    attrs {
        this.flipId = flipId
        this.inverseFlipId = inverseFlipId
    }
    handler()
}
