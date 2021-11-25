package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.minreact.reactFunction
import react.RBuilder
import react.Props

inline fun <reified P : Props> windowReactFunc(crossinline handler: RBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        reactFunction<P> {
            handler(
                it,
                windowFunctions
            )
        }
    }