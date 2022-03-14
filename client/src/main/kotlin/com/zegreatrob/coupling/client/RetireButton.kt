package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import com.zegreatrob.minreact.child
import csstype.ClassName
import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(className: ClassName, onRetire: () -> Unit) =
    child(CouplingButton(small, red, className, onRetire)) {
        +"Retire"
    }
