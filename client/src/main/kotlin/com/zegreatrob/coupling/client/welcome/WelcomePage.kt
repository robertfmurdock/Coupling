package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.DecoratedDispatchFunc
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoadWrapper
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction

private val LoadedWelcome = couplingDataLoadWrapper(Welcome)

val WelcomePage = reactFunction<PageProps> { props ->
    child(LoadedWelcome, dataLoadProps { bridge ->
        WelcomeProps(DecoratedDispatchFunc(props.commander::tracingDispatcher, bridge))
    })
}
