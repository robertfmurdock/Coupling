package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.couplingDataLoader
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.minreact.child
import react.FC

private val LoadedWelcome = couplingDataLoader<Welcome>()

val WelcomePage = FC<PageProps> {
    child(dataLoadProps(LoadedWelcome) { Welcome() })
}
