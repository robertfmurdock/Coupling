package com.zegreatrob.coupling.client

data class Controls<out D>(
    val dispatchFunc: DispatchFunc<out D>,
    val reload: () -> Unit
)
