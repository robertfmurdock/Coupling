package com.zegreatrob.coupling.client.components

import react.ChildrenBuilder
import react.dom.html.ButtonHTMLAttributes
import web.html.ButtonType

fun ChildrenBuilder.configSaveButton(isSaving: Boolean) = CouplingButton(
    sizeRuleSet = supersize,
    colorRuleSet = blue,
    attrs = fun ButtonHTMLAttributes<*>.() {
        type = ButtonType.submit
        tabIndex = 0
        value = "Save"
        disabled = isSaving
    },
) {
    +"Save"
}
