package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder
import react.RBuilder

inline fun <reified P : DataProps<P>> windowReactFunc(crossinline handler: RBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        reactFunction<P> { handler(it, windowFunctions) }
    }

inline fun <reified P : DataProps<P>> windowTmFC(crossinline handler: ChildrenBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        tmFC<P> { handler(it, windowFunctions) }
    }

