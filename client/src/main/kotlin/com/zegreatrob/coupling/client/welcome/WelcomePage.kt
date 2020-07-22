package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction

private val LoadedWelcome = dataLoadWrapper(Welcome)

val WelcomePage = reactFunction<PageProps> { props ->
    child(LoadedWelcome, dataLoadProps { _, scope ->
        WelcomeProps(DecoratedDispatchFunc(props.commander::tracingDispatcher, scope))
    })
}
