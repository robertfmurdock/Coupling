@file:JsModule("react-flip-toolkit")

package com.zegreatrob.coupling.client.external.reactfliptoolkit

import react.ElementType
import react.Props

@JsName("Flipper")
external val Flipper: ElementType<FlippeProps>

external interface FlippeProps : Props {
    var flipKey: String
    var className: String?
}

@JsName("Flipped")
external val Flipped: ElementType<FlippedProps>

external interface FlippedProps : Props {
    var flipId: String?
    var inverseFlipId: String?
}
