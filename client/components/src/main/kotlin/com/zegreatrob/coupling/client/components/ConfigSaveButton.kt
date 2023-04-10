package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import web.html.ButtonType

fun ChildrenBuilder.configSaveButton(isSaving: Boolean) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(
        sizeRuleSet = com.zegreatrob.coupling.client.components.supersize,
        colorRuleSet = com.zegreatrob.coupling.client.components.blue,
        attrs = {
            type = ButtonType.submit
            tabIndex = 0
            value = "Save"
            disabled = isSaving
        },
    ),
) {
    +"Save"
}
