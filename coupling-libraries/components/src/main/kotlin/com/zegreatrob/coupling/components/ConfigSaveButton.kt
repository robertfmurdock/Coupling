package com.zegreatrob.coupling.components

import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import web.html.ButtonType

fun ChildrenBuilder.configSaveButton(isSaving: Boolean) = add(
    CouplingButton(
        sizeRuleSet = supersize,
        colorRuleSet = blue,
        attrs = {
            type = ButtonType.submit
            tabIndex = 0
            value = "Save"
            disabled = isSaving
        }
    )
) {
    +"Save"
}
