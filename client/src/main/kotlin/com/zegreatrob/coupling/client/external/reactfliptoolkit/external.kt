@file:JsModule("react-flip-toolkit")

package com.zegreatrob.coupling.client.external.reactfliptoolkit;

import react.RClass
import react.RProps

@JsName("Flipper")
external val Flipper: RClass<FlipperProps>

external interface FlipperProps : RProps {
    var flipKey: String
    var className: String?
}

@JsName("Flipped")
external val Flipped: RClass<FlippedProps>

external interface FlippedProps : RProps {
    var flipId: String?
    var inverseFlipId: String?
}
