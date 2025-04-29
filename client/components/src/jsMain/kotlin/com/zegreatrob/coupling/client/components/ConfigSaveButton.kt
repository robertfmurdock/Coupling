package com.zegreatrob.coupling.client.components

import js.objects.unsafeJso
import react.ChildrenBuilder
import web.html.ButtonType

fun ChildrenBuilder.configSaveButton(isSaving: Boolean) = CouplingButton {
    sizeRuleSet = supersize
    colorRuleSet = blue
    buttonProps = unsafeJso {
        type = ButtonType.submit
        tabIndex = 0
        value = "Save"
        disabled = isSaving
    }
    +"Save"
}
