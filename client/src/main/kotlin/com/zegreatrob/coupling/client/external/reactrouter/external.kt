@file:JsModule("react-router-dom")
@file:JsNonModule
package com.zegreatrob.coupling.client.external.reactrouter

import react.RClass
import react.RProps

@JsName("Prompt")
external val PromptComponent : RClass<PromptProps>

external interface PromptProps : RProps {
    var `when`: Boolean
    var message: String
}
