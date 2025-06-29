package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.client.components.DispatchFunc

data class PageFunctions(val reload: () -> Unit, val dispatchFunc: DispatchFunc<ClientDispatcher>)
