package com.zegreatrob.coupling.client

import kotlinx.html.ButtonType
import kotlinx.html.classes
import kotlinx.html.tabIndex
import react.RBuilder
import react.dom.button

fun RBuilder.configSaveButton(isSaving: Boolean, className: String) = button(classes = "super blue button") {
    attrs {
        classes += className
        type = ButtonType.submit
        tabIndex = "0"
        value = "Save"
        disabled = isSaving
    }
    +"Save"
}