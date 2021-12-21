package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.minreact.DataProps
import react.RBuilder

inline fun <reified P : DataProps<P>> windowReactFunc(crossinline handler: RBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        reactFunction<P> { handler(it, windowFunctions) }
    }