@file:JsModule("react-flip-toolkit")

package com.zegreatrob.coupling.client.components.external.reactfliptoolkit

import react.ElementType
import react.PropsWithChildren
import react.PropsWithClassName

@JsName("Flipper")
external val Flipper: ElementType<FlipperProps>

external interface FlipperProps : PropsWithClassName, PropsWithChildren {
    var flipKey: String
}

@JsName("Flipped")
external val Flipped: ElementType<FlippedProps>

external interface FlippedProps : PropsWithChildren {
    var flipId: String?
    var inverseFlipId: String?
}
