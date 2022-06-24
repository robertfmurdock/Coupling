@file:JsModule("react-flip-toolkit")

package com.zegreatrob.coupling.components.external.reactfliptoolkit

import react.ElementType
import react.Props
import react.PropsWithClassName

@JsName("Flipper")
external val Flipper: ElementType<FlipperProps>

external interface FlipperProps : PropsWithClassName {
    var flipKey: String
}

@JsName("Flipped")
external val Flipped: ElementType<FlippedProps>

external interface FlippedProps : Props {
    var flipId: String?
    var inverseFlipId: String?
}
