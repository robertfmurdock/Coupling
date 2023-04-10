package com.zegreatrob.coupling.components

data class Controls<out D>(
    val dispatchFunc: DispatchFunc<out D>,
    val reload: () -> Unit,
)
