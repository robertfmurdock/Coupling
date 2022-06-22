package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.red
import com.zegreatrob.coupling.components.small
import com.zegreatrob.minreact.add
import csstype.ClassName
import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(className: ClassName, onRetire: () -> Unit) =
    add(CouplingButton(small, red, className, onRetire)) {
        +"Retire"
    }
