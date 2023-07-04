package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(onRetire: () -> Unit) = add(CouplingButton(small, red, onClick = onRetire)) {
    +"Retire"
}
