package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.CouplingButton
import com.zegreatrob.coupling.components.red
import com.zegreatrob.coupling.components.small
import com.zegreatrob.minreact.add
import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(onRetire: () -> Unit) = add(CouplingButton(small, red, onClick = onRetire)) {
    +"Retire"
}
