package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RBuilder

fun RBuilder.retireButton(className: String, onRetire: () -> Unit) = couplingButton(small, red) {
    attrs {
        classes += className
        onClickFunction = { onRetire() }
    }
    +"Retire"
}