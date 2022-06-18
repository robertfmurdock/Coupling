package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.red
import com.zegreatrob.coupling.client.dom.small
import com.zegreatrob.minreact.create
import csstype.ClassName
import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(className: ClassName, onRetire: () -> Unit) =
    +CouplingButton(small, red, className, onRetire).create {
        +"Retire"
    }
