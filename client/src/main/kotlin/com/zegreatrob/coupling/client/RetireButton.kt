package com.zegreatrob.coupling.client

import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.div

fun RBuilder.retireButton(onRetire: () -> Unit, className: String) = div(classes = "small red button") {
    attrs {
        classes += className
        onClickFunction = { onRetire() }
    }
    +"Retire"
}