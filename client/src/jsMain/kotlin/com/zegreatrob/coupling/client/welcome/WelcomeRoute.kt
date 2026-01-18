package com.zegreatrob.coupling.client.welcome

import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.configContext
import com.zegreatrob.coupling.client.routing.CouplingRoute
import react.FC
import react.use

val WelcomeRoute = FC {
    val config: ClientConfig = use(configContext)
    CouplingRoute(WelcomePage, "Welcome", config)
}
