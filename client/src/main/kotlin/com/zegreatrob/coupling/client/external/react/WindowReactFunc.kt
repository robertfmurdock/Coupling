package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.react.external.react.reactFunction
import react.RBuilder
import react.RProps

inline fun <reified P : RProps> windowReactFunc(crossinline handler: RBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        reactFunction<P> {
            handler(
                it,
                windowFunctions
            )
        }
    }