package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.add
import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(onRetire: () -> Unit) = add(
    com.zegreatrob.coupling.client.components.CouplingButton(
        com.zegreatrob.coupling.client.components.small,
        com.zegreatrob.coupling.client.components.red,
        onClick = onRetire,
    ),
) {
    +"Retire"
}
