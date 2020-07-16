package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.builder
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import react.RBuilder

private val LoadedWelcome = dataLoadWrapper(Welcome)
private val RBuilder.loadedWelcome get() = this.builder(LoadedWelcome)

val WelcomePage = reactFunction<PageProps> { props ->
    loadedWelcome(DataLoadProps { _, scope ->
        WelcomeProps(
            DecoratedDispatchFunc(props.commander::tracingDispatcher, scope)
        )
    })
}
