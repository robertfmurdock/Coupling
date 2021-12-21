package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps

private val LoadedWelcome = couplingDataLoader(Welcome)

val WelcomePage = reactFunction<PageProps> { _ ->
    child(LoadedWelcome, dataLoadProps { _ -> WelcomeProps() })
}
