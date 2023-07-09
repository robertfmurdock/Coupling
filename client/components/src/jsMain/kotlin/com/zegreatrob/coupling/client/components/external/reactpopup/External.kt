@file:JsModule("reactjs-popup")

package com.zegreatrob.coupling.client.components.external.reactpopup

import react.ElementType
import react.PropsWithClassName
import react.ReactNode
import kotlin.js.Json

external val default: ElementType<PopupProps>

external interface PopupProps : PropsWithClassName {
    var modal: Boolean?
    var trigger: (Boolean) -> ReactNode
    var open: Boolean?
    var on: Array<String>
    var contentStyle: Json?
    var onOpen: () -> Unit
    var onClose: () -> Unit
}
