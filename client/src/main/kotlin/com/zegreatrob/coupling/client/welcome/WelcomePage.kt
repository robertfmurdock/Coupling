package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.external.react.child
import com.zegreatrob.coupling.client.routing.DataLoadProps
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.minreact.reactFunction

private val LoadedWelcome = dataLoadWrapper(Welcome)

val WelcomePage = reactFunction<PageProps> { props ->
    child(LoadedWelcome, DataLoadProps { _, scope ->
        WelcomeProps(
            DecoratedDispatchFunc(props.commander::tracingDispatcher, scope)
        )
    })
}
