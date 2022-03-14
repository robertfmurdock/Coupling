package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.minreact.child
import csstype.ClassName
import kotlinx.html.ButtonType
import kotlinx.html.tabIndex
import react.ChildrenBuilder

fun ChildrenBuilder.configSaveButton(isSaving: Boolean, className: ClassName) = child(
    CouplingButton(
        sizeRuleSet = supersize,
        colorRuleSet = blue,
        className = className,
        attrs = {
            type = ButtonType.submit
            tabIndex = "0"
            value = "Save"
            disabled = isSaving
        })
) {
    +"Save"
}
