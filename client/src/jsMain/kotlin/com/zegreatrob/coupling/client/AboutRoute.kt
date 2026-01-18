package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.routing.CouplingRoute
import react.FC
import react.use

val AboutRoute = FC {
    val config: ClientConfig = use(configContext)
    CouplingRoute(AboutPage, "About", config)
}
