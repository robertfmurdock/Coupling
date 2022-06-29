package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.blue
import com.zegreatrob.coupling.components.supersize
import com.zegreatrob.minreact.add
import react.ChildrenBuilder
import react.dom.html.ButtonType

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
