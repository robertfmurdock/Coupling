package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.blue
import com.zegreatrob.coupling.client.dom.supersize
import com.zegreatrob.minreact.create
import csstype.ClassName
import react.ChildrenBuilder

fun ChildrenBuilder.configSaveButton(isSaving: Boolean, className: ClassName) = +CouplingButton(
    sizeRuleSet = supersize,
    colorRuleSet = blue,
    className = className,
    attrs = {
        type = react.dom.html.ButtonType.submit
        tabIndex = 0
        value = "Save"
        disabled = isSaving
    }
).create {
    +"Save"
}
