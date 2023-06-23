package com.zegreatrob.coupling.client.integration

import com.zegreatrob.coupling.client.components.ConfigFrame
import com.zegreatrob.coupling.client.partyPageFunction
import com.zegreatrob.coupling.client.routing.CouplingQuery
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.minreact.create

val IntegrationPage = partyPageFunction { props, partyId ->
    ConfigFrame {
        +CouplingQuery(
            commander = props.commander,
            query = graphQuery {
                party(partyId) {
                    party()
                    integration()
                }
            },
            toDataprops = { _, _, result ->
                IntegrationContent(
                    party = result.party?.details?.data ?: return@CouplingQuery null,
                    integration = result.party?.integration?.data,
                )
            },
        ).create(key = partyId.value)
    }
}
