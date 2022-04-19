@file:JsModule("react-router-dom")

package com.zegreatrob.coupling.client.external.reactrouter

import react.ElementType
import react.Props

@JsName("Prompt")
external val PromptComponent: ElementType<PromptProps>

external interface PromptProps : Props {
    var `when`: Boolean?
    var message: String
}
