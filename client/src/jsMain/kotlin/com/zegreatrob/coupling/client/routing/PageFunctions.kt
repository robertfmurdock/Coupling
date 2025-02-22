package com.zegreatrob.coupling.client.routing

import com.zegreatrob.coupling.client.ClientDispatcher
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.react.dataloader.ReloadFunc

data class PageFunctions(val reload: ReloadFunc, val dispatchFunc: DispatchFunc<ClientDispatcher>)
