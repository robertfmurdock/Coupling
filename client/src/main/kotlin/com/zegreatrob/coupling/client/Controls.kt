package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.DispatchFunc

data class Controls<out D>(
    val dispatchFunc: DispatchFunc<out D>,
    val reload: () -> Unit
)
