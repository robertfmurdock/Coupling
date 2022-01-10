@file:JsModule("reactjs-popup")

package com.zegreatrob.coupling.client.external.reactpopup

import react.ElementType
import react.PropsWithClassName
import react.ReactElement
import kotlin.js.Json

external val default: ElementType<PopupProps>

external interface PopupProps : PropsWithClassName {
    var modal: Boolean?
    var trigger: (Boolean) -> ReactElement
    var open: Boolean?
    var on: Array<String>
    var contentStyle: Json?
}
