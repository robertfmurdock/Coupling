package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.tmFC
import react.ChildrenBuilder

inline fun <reified P : DataProps<P>> windowReactFunc(crossinline handler: ChildrenBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        tmFC<P> { handler(it, windowFunctions) }
    }

inline fun <reified P : DataProps<P>> windowTmFC(crossinline handler: ChildrenBuilder.(P, WindowFunctions) -> Unit) =
    { windowFunctions: WindowFunctions ->
        tmFC<P> { handler(it, windowFunctions) }
    }
