package com.zegreatrob.coupling.client.components

import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(onRetire: () -> Unit) = CouplingButton {
    sizeRuleSet = small
    colorRuleSet = red
    onClick = onRetire
    +"Retire"
}
