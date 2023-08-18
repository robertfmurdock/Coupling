package com.zegreatrob.coupling.client.components

data class Controls<out D>(
    val dispatchFunc: DispatchFunc<D>,
    val reload: () -> Unit,
)
