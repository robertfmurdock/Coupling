package com.zegreatrob.coupling.client.party

import com.zegreatrob.coupling.client.ClientConfig
import com.zegreatrob.coupling.client.configContext
import com.zegreatrob.coupling.client.routing.CouplingRoute
import react.FC
import react.use

val PartyListRoute = FC {
    val config: ClientConfig = use(configContext)
    CouplingRoute(PartyListPage, "Party List", config)
}
