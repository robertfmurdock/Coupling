package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import react.fc

private val LoadedWelcome = couplingDataLoader<Welcome>()

val WelcomePage = fc<PageProps> {
    child(dataLoadProps(LoadedWelcome) { Welcome() })
}
