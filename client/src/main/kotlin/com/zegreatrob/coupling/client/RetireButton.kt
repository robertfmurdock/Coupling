package com.zegreatrob.coupling.client

import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.div

fun RBuilder.retireButton(className: String, onRetire: () -> Unit) = div(classes = "small red button") {
    attrs {
        classes += className
        onClickFunction = { onRetire() }
    }
    +"Retire"
}