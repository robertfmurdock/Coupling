package com.zegreatrob.coupling.client.demo

import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.configContext
import com.zegreatrob.coupling.client.routing.CouplingRoute
import react.FC
import react.use

val DemoRoute = FC {
    val config: ClientConfig = use(configContext)
    CouplingRoute(DemoPage, "Demo", config)
}
