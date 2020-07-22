package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.supersize
import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.tabIndex
import react.RBuilder

fun RBuilder.configSaveButton(isSaving: Boolean, className: String) = couplingButton(supersize, blue) {
    attrs {
        classes += className
        type = ButtonType.submit
        tabIndex = "0"
        value = "Save"
        disabled = isSaving
    }
    +"Save"
}
