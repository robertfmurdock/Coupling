@file:JsModule("reactjs-popup")

package com.zegreatrob.coupling.client.external.reactpopup

import react.RClass
import react.ReactElement
import react.dom.WithClassName
import kotlin.js.Json

external val default: RClass<PopupProps>

external interface PopupProps : WithClassName {
    var modal: Boolean
    var trigger: (Boolean) -> ReactElement
    var open: Boolean?
    var on: Array<String>
    var contentStyle: Json
}
