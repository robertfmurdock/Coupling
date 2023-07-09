package com.zegreatrob.coupling.client.components

import react.ChildrenBuilder

fun ChildrenBuilder.retireButton(onRetire: () -> Unit) = CouplingButton(small, red, onClick = onRetire) { +"Retire" }
